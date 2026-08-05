package sn.smartwaste.collect.config.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Un statut HTTP choisi délibérément par un contrôleur doit survivre jusqu'au client.
 *
 * <p><b>Le défaut fermé ici.</b> Le fourre-tout {@code @ExceptionHandler(Exception.class)}
 * capturait aussi les {@link org.springframework.web.server.ResponseStatusException} — l'idiome
 * même par lequel un contrôleur dit « je sais exactement quel statut rendre ». Les <b>29</b>
 * statuts délibérés du projet, répartis dans 11 classes, étaient donc tous aplatis en 500 :
 * requête malformée, conflit, clé de capteur invalide, ressource absente.
 *
 * <p>Les conséquences ne sont pas cosmétiques. Un capteur dont la clé est refusée recevait 500 :
 * il ne pouvait pas distinguer « ma clé est mauvaise » de « le serveur est tombé », et réessayait
 * indéfiniment. Côté exploitation, chaque requête client malformée apparaissait comme un incident
 * serveur dans les journaux, noyant les vrais.
 *
 * <p>C'est la troisième fois que ce fourre-tout masque un statut : le jeton expiré rendait 500 au
 * lieu de 401 (ADR-0003), le refus d'autorisation 500 au lieu de 403. Chacun avait été corrigé
 * <i>séparément</i>. Celui-ci ferme la famille entière.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:adminauthz;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "sonaged.deletion.purge-cron=0 0 5 31 2 ?",
        "sonaged.alerts.stream.heartbeat-ms=3600000",
        "sonaged.collection.reminder.cron=0 0 5 31 2 ?"
})
class ResponseStatusPreservedTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("une periode inversee est une erreur de requete, pas une panne serveur")
    void badRequestStaysBadRequest() throws Exception {
        mockMvc.perform(get("/v1/supervision/reports")
                        .param("from", "2026-08-01T00:00:00Z")
                        .param("to", "2026-07-01T00:00:00Z"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("une cle de capteur absente rend 401, et non 500")
    void unauthorizedStaysUnauthorized() throws Exception {
        // Le cas qui coute le plus cher en exploitation : un capteur qui recoit 500 ne peut pas
        // savoir que sa cle est en cause, et reessaie indefiniment.
        mockMvc.perform(post("/v1/measurements").with(csrf())
                        .contentType("application/json").content("{\"fillLevelPercent\":50}"))
                .andExpect(status().isUnauthorized());
    }
}
