package sn.smartwaste.collect.platform.presentation.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autorisation sur les signalements citoyens.
 *
 * <p><b>Pourquoi ce test passe par le contexte Spring complet</b> plutôt que par des mocks : les
 * annotations {@code @PreAuthorize} ne font <b>rien</b> tant que {@code @EnableMethodSecurity} n'est
 * pas déclaré. Un test unitaire sur le contrôleur passerait donc au vert avec ou sans protection.
 * Le seul moyen de vérifier que la règle s'applique vraiment est de traverser la chaîne de sécurité.
 *
 * <p>Ce n'est pas une précaution théorique : ce projet contient déjà treize règles d'autorisation
 * ({@code AuthorityRules}, {@code UserRules}) que rien n'applique, et qui donnent depuis le début
 * l'illusion d'un contrôle d'accès.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:avisauthz;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
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
class AvisAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("un habitant ne peut pas faire avancer un signalement")
    void citizenCannotChangeStatus() throws Exception {
        // Sans cette règle, n'importe quel compte pouvait clore ou rejeter le signalement d'autrui.
        mockMvc.perform(put("/avis/1/statut/EN_COURS").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("un habitant ne peut pas lire la file de traitement ni la carte des signalements")
    void citizenCannotListEveryReport() throws Exception {
        mockMvc.perform(get("/avis")).andExpect(status().isForbidden());
        mockMvc.perform(get("/avis/map")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("l'encadrement accède à la file de traitement")
    void supervisorCanListQueue() throws Exception {
        mockMvc.perform(get("/avis")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("l'encadrement n'est pas bloqué par l'autorisation sur la transition")
    void supervisorIsNotForbiddenOnTransition() throws Exception {
        // Le signalement 999 n'existe pas : la réponse sera une erreur métier, pas un 403.
        // C'est bien l'autorisation qu'on vérifie ici, pas le résultat fonctionnel.
        int status = mockMvc.perform(put("/avis/999/statut/EN_COURS").with(csrf()))
                .andReturn().getResponse().getStatus();
        assertThat(status).isNotEqualTo(HttpStatus.FORBIDDEN.value());
    }

    /** CSRF est désactivé dans la chaîne, mais l'ajouter rend le test robuste s'il était réactivé. */
    private static org.springframework.test.web.servlet.request.RequestPostProcessor csrf() {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf();
    }
}
