package sn.smartwaste.collect.identity.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autorisation sur la surface d'administration.
 *
 * <p><b>Le défaut fermé ici.</b> La chaîne s'arrêtait à {@code anyRequest().authenticated()} :
 * <i>tout compte authentifié pouvait tout faire</i>. Comme {@code /auth/register} est public, le
 * chemin complet était ouvert à n'importe qui — s'inscrire, activer son compte, puis écraser le
 * référentiel territorial, réimporter les GeoJSON, créer d'autres comptes ou fouiller la corbeille.
 * Les treize règles de {@code AuthorityRules} / {@code UserRules} donnaient l'illusion du contraire :
 * rien ne les applique.
 *
 * <p><b>Pourquoi le contexte complet.</b> Une règle d'autorisation qui n'est pas câblée ne proteste
 * pas — c'est précisément le mode de panne de ce projet. Seule la traversée réelle de la chaîne de
 * sécurité distingue « protégé » de « paraît protégé ».
 *
 * <p>Les cas se lisent en deux moitiés, et la seconde compte autant que la première : refuser trop
 * large casserait l'application mobile et la carte, dont le contenu est justement fait pour être
 * consulté.
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
class AdministrationAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------------------------------------------------------------- ce qui est refusé

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("un habitant ne peut plus écrire dans le référentiel ni dans le parc")
    void citizenCannotWriteTheReferential() throws Exception {
        // Chemin d'exploitation complet avant correctif : /auth/register est public.
        mockMvc.perform(post("/v1/depotoirs").with(csrf()).contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/v1/communes/00000000-0000-0000-0000-000000000001").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("un habitant ne peut pas réimporter les GeoJSON de référence")
    void citizenCannotReimportReferenceData() throws Exception {
        // L'import écrase le référentiel en bloc : c'est l'écriture la plus destructrice de l'API.
        mockMvc.perform(post("/v1/admin/import/geojson").with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/data/depotoir").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("un habitant ne voit ni les comptes, ni les rôles, ni la corbeille")
    void citizenCannotReadAdministration() throws Exception {
        // La lecture est réservée elle aussi : la liste des comptes et l'état de la corbeille
        // renseignent sur l'organisation autant que les écritures la modifient.
        mockMvc.perform(get("/v1/users/s")).andExpect(status().isForbidden());
        mockMvc.perform(get("/v1/authorities")).andExpect(status().isForbidden());
        mockMvc.perform(get("/v1/deletions")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("un habitant ne lit pas les rapports de supervision ni le flux d'alertes")
    void citizenCannotReadSupervision() throws Exception {
        mockMvc.perform(get("/v1/supervision/stats")).andExpect(status().isForbidden());
        mockMvc.perform(get("/v1/alerts/stream")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("un endpoint d'écriture non énuméré est fermé par défaut")
    void unlistedWritesAreClosedByDefault() throws Exception {
        // Le point de la règle : protéger par énumération, c'est accepter d'en oublier une — et
        // celle qu'on oublie ne proteste pas. Ici le défaut est le refus.
        mockMvc.perform(post("/v1/moblier-urbains").with(csrf()).contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/v1/typedepotoirs").with(csrf()).contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------ ce qui doit rester accessible

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("la lecture du référentiel reste ouverte : c'est le contenu de l'application mobile")
    void referentialReadsStayOpen() throws Exception {
        assertNotForbidden(get("/v1/communes/s"));
        assertNotForbidden(get("/v1/depotoirs/s"));
        assertNotForbidden(get("/v1/maps/depotoirs"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("les gestes d'habitant ne sont pas emportés par le refus par défaut")
    void citizenActionsStillWork() throws Exception {
        // `/v1/collection-subscriptions` est une écriture sous `/v1` : sans exception explicite,
        // le refus par défaut aurait supprimé l'abonnement aux rappels de collecte.
        assertNotForbidden(post("/v1/collection-subscriptions").with(csrf())
                .contentType("application/json").content("{\"quartierId\":null}"));
        assertNotForbidden(post("/avis").with(csrf())
                .contentType("application/json").content("{\"message\":\"Depot sauvage\"}"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("un capteur transmet toujours sans compte utilisateur")
    void deviceIngestionRemainsOpenToTheChain() throws Exception {
        // Un capteur n'est pas une personne : il s'authentifie par clé de device, vérifiée par le
        // service. Lui imposer un rôle reviendrait à couper l'ingestion.
        assertNotForbidden(post("/v1/measurements").with(csrf())
                .contentType("application/json").content("{}"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("la documentation reste consultable par sa porte d'entrée réelle")
    void swaggerEntryPointStaysReachable() throws Exception {
        // `/swagger-ui.html` n'est pas un fichier de `/swagger-ui/` : c'est le chemin que springdoc
        // expose par défaut, et qui redirige vers `/swagger-ui/index.html`. Le motif `/swagger-ui/**`
        // ne le couvrait pas, si bien que la redirection était refusée en 403 avant d'avoir lieu :
        // la documentation n'était atteignable qu'en devinant l'URL d'arrivée. Les deux chemins sont
        // vérifiés ici, faute de quoi la même distinction se reperdra.
        assertNotForbidden(get("/swagger-ui.html"));
        assertNotForbidden(get("/swagger-ui/index.html"));
        assertNotForbidden(get("/sonaged-docs"));
        assertNotForbidden(get("/sonaged-docs/swagger-config"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("l'administration accède à ce dont elle a la charge")
    void administrationIsNotLockedOut() throws Exception {
        mockMvc.perform(get("/v1/users/s")).andExpect(status().isOk());
        mockMvc.perform(get("/v1/deletions")).andExpect(status().isOk());
        mockMvc.perform(get("/v1/supervision/stats")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("SUPER_ADMIN est habilité au même titre qu'ADMIN")
    void superAdminIsAlsoAdministration() throws Exception {
        // Le rôle existe et est semé par le changelog 2.1.0 : l'oublier dans `hasAnyRole`
        // verrouillerait le compte le plus privilégié hors de sa propre application.
        mockMvc.perform(get("/v1/users/s")).andExpect(status().isOk());
        assertNotForbidden(post("/v1/depotoirs").with(csrf())
                .contentType("application/json").content("{}"));
    }

    /**
     * Vérifie l'autorisation, pas le résultat fonctionnel : ces requêtes portent des corps vides ou
     * des identifiants inexistants et échoueront plus loin. Seul le 403 est disqualifiant.
     */
    private void assertNotForbidden(org.springframework.test.web.servlet.RequestBuilder request) throws Exception {
        int status = mockMvc.perform(request).andReturn().getResponse().getStatus();
        assertThat(status).isNotEqualTo(HttpStatus.FORBIDDEN.value());
    }
}
