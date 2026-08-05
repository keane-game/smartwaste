package sn.smartwaste.collect.administration.infrastructure.bootstrap;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import sn.smartwaste.collect.identity.application.api.AdminAccountProvisioning;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Déclenchement de l'amorçage du compte d'administration (ADR-0014 §2).
 *
 * <p>La création elle-même appartient à l'identité et se vérifie ailleurs
 * ({@code AdminAccountProvisioningImplTest}). Ce qui se joue <b>ici</b> est la condition de
 * déclenchement, et elle tient en une phrase : sans identifiants dans l'environnement, on ne crée
 * rien et on ne devine rien. Un mot de passe par défaut serait le secret versionné qu'ADR-0002
 * proscrit — figé, partagé par tous les déploiements, et impossible à roter sans livraison.
 */
class AdminBootstrapRunnerTest {

    private static final String EMAIL = "admin@sonaged.sn";
    private static final String MOT_DE_PASSE = "un-mot-de-passe-fourni-par-l-environnement";

    private final RecordingProvisioning provisioning = new RecordingProvisioning();

    private AdminBootstrapRunner runner(boolean enabled, String email, String password) {
        return new AdminBootstrapRunner(provisioning, enabled, email, password);
    }

    @Test
    @DisplayName("sans identifiants dans l'environnement, l'amorçage n'est pas tenté")
    void doesNothingWithoutCredentials() {
        // Le démarrage ne doit pas échouer pour autant : un environnement peut légitimement gérer
        // ses comptes autrement (recette restaurée, Keycloak à terme).
        runner(true, "", "").run(null);

        assertThat(provisioning.appels).isEmpty();
    }

    @Test
    @DisplayName("un mot de passe sans adresse ne suffit pas")
    void doesNothingWithAPartialConfiguration() {
        runner(true, "", MOT_DE_PASSE).run(null);

        assertThat(provisioning.appels).isEmpty();
    }

    @Test
    @DisplayName("avec les deux identifiants, l'amorçage est demandé tel quel")
    void delegatesWhenConfigured() {
        runner(true, EMAIL, MOT_DE_PASSE).run(null);

        assertThat(provisioning.appels).containsExactly(EMAIL + "/" + MOT_DE_PASSE);
    }

    @Test
    @DisplayName("l'amorçage se désactive par configuration")
    void canBeDisabled() {
        runner(false, EMAIL, MOT_DE_PASSE).run(null);

        assertThat(provisioning.appels).isEmpty();
    }

    /** Enregistre ce que le runner demande au port, sans rejouer la logique de l'identité. */
    private static final class RecordingProvisioning implements AdminAccountProvisioning {
        private final List<String> appels = new ArrayList<>();

        @Override
        public Outcome createAdministratorIfAbsent(String email, String rawPassword) {
            appels.add(email + "/" + rawPassword);
            return Outcome.CREATED;
        }
    }
}
