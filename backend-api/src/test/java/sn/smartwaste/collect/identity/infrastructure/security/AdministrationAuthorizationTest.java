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
 * Les treize règles de {@code AuthorityRules} / {@code UserRules} (retirées le 2026-08-06)
 * donnaient l'illusion du contraire : rien ne les appliquait jamais.
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
        // Creer une campagne ou un quiz reste reserve a l'administration, comme un message isole.
        mockMvc.perform(post("/v1/awareness/campaigns").with(csrf()).contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/v1/quizzes").with(csrf()).contentType("application/json").content("{}"))
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
        // Meme risque que pour /avis et /v1/collection-subscriptions : POST /v1/** est ferme par
        // defaut, et repondre a un quiz est un geste d'habitant qui doit passer malgre tout.
        assertNotForbidden(post("/v1/quizzes/00000000-0000-0000-0000-000000000001/answers")
                .with(csrf()).contentType("application/json").content("[]"));
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
    @WithMockUser(authorities = {"ROLE_AGENT", "DECLARE_COLLECTION"})
    @DisplayName("celui qui porte DECLARE_COLLECTION peut declarer ses passages")
    void collectionAgentMayDeclarePassages() throws Exception {
        // Le defaut ferme ici : `@PreAuthorize` autorisait bien l'agent sur ces deux methodes, mais
        // la chaine de filtres tranche AVANT la securite de methode, et la regle generale
        // « POST /v1/** reserve a l'administration » le refusait — sur sa propre commune. Aucun
        // test unitaire ne pouvait le voir : le service et son annotation etaient justes, c'est
        // l'ordre des deux mecanismes qui ne l'etait pas.
        assertNotForbidden(post("/v1/collection-routes/stops/00000000-0000-0000-0000-000000000001/collected").with(csrf()));
        assertNotForbidden(post("/v1/collection-routes/stops/00000000-0000-0000-0000-000000000001/inaccessible").with(csrf())
                .contentType("application/json").content("{\"reason\":\"voie barree\"}"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_AGENT")
    @DisplayName("le role seul ne suffit plus : c'est la permission qui autorise")
    void roleAloneNoLongerGrants() throws Exception {
        // Le coeur du changement. L'autorisation exigeait `hasAnyRole('AGENT',…)` — une liste de
        // roles figee dans le code. Elle se lit desormais dans les permissions du role, que
        // l'administration modifie en base : retirer DECLARE_COLLECTION suffit a retirer le droit,
        // sans livraison.
        mockMvc.perform(post("/v1/collection-routes/stops/00000000-0000-0000-0000-000000000001/collected").with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/v1/collection-routes").param("communeId",
                        "00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_AGENT", "DECLARE_COLLECTION", "VIEW_COLLECTION_ROUTE"})
    @DisplayName("l'agent reste tenu a l'ecart du reste de l'ecriture")
    void collectionAgentStaysOutOfTheRest() throws Exception {
        // L'ouverture doit rester limitee aux deux chemins nommes : un agent n'administre pas le
        // referentiel, ne cree pas de comptes et n'enrole pas d'equipements.
        mockMvc.perform(post("/v1/depotoirs").with(csrf())
                        .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/v1/devices/sensors").with(csrf())
                        .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/v1/users/s")).andExpect(status().isForbidden());
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

    @Test
    @WithAnonymousUser
    @DisplayName("changer son mot de passe exige une identite, meme si /auth/** est public par defaut")
    void changePasswordRequiresAuthentication() throws Exception {
        // ADR-0021 : seule exception au permitAll de /auth/** — un jeton d'acces expire ou absent
        // ne doit pas suffire a changer le mot de passe d'un compte quelconque.
        mockMvc.perform(post("/auth/change-password").with(csrf())
                        .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("un habitant ne voit pas le catalogue des permissions")
    void citizenCannotReadPermissions() throws Exception {
        mockMvc.perform(get("/v1/permissions")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "MANAGE_ROLE"})
    @DisplayName("qui porte MANAGE_ROLE lit le catalogue des permissions")
    void manageRoleCanReadPermissions() throws Exception {
        mockMvc.perform(get("/v1/permissions")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISEUR")
    @DisplayName("le superviseur lit la supervision mais n'administre pas")
    void supervisorReadsSupervisionButNotAdministration() throws Exception {
        // 2.21.0 : ouvert pour VIEW_SUPERVISION, sans lui donner /v1/users, /v1/authorities ni
        // /v1/admin — ce bloc reste réservé à ADMIN/SUPER_ADMIN (voir le matcher juste au-dessus).
        mockMvc.perform(get("/v1/supervision/stats")).andExpect(status().isOk());
        mockMvc.perform(get("/v1/users/s")).andExpect(status().isForbidden());
        mockMvc.perform(get("/v1/admin/import/geojson")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_TECHNICIEN_IOT", "MANAGE_DEVICES"})
    @DisplayName("le technicien IoT enrôle un capteur mais n'administre pas le référentiel")
    void iotTechnicianManagesDevicesButNotTheReferential() throws Exception {
        // 2.21.0 : la règle générale « POST /v1/** réservée à l'administration » l'aurait exclu
        // avant même d'atteindre le `@PreAuthorize(MANAGE_DEVICES)` du contrôleur — même défaut
        // que celui documenté plus haut pour l'agent de collecte (l'ordre filtre/méthode compte).
        assertNotForbidden(post("/v1/devices/sensors").with(csrf())
                .contentType("application/json").content("{}"));
        mockMvc.perform(post("/v1/depotoirs").with(csrf())
                        .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
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
