package sn.smartwaste.collect.administration.infrastructure.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import sn.smartwaste.collect.identity.application.api.AdminAccountProvisioning;

/**
 * Déclenche l'amorçage du compte d'administration au démarrage (ADR-0014 §2).
 *
 * <p><b>Ce composant décide « quand », pas « comment ».</b> Créer un compte, lui attribuer un rôle
 * et hacher un mot de passe appartiennent à l'identité : ce runner passe donc par le port publié
 * {@link AdminAccountProvisioning}. Une première version manipulait directement
 * {@code UserRepository} et {@code UserEntity} — {@code modules.verify()} l'a refusée, à raison
 * (ADR-0013 §3 : aucun accès au repository d'un autre contexte).
 *
 * <p><b>Les identifiants viennent de l'environnement, sans valeur par défaut.</b> Un mot de passe
 * d'amorçage inscrit dans le dépôt serait un secret versionné (ADR-0002), figé et partagé par tous
 * les déploiements. Absence de configuration = aucun compte créé et un avertissement explicite : le
 * démarrage n'échoue pas, car un environnement peut légitimement gérer ses comptes autrement
 * (recette restaurée, Keycloak à terme).
 */
@Component
@Slf4j
// Avant l'import GeoJSON : sans compte, la voie HTTP de l'import reste fermée.
@Order(0)
public class AdminBootstrapRunner implements ApplicationRunner {

    private final AdminAccountProvisioning provisioning;
    private final boolean enabled;
    private final String email;
    private final String password;

    public AdminBootstrapRunner(
            AdminAccountProvisioning provisioning,
            @Value("${sonaged.bootstrap.admin.enabled:true}") boolean enabled,
            @Value("${sonaged.bootstrap.admin.email:}") String email,
            @Value("${sonaged.bootstrap.admin.password:}") String password) {
        this.provisioning = provisioning;
        this.enabled = enabled;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        if (isBlank(email) || isBlank(password)) {
            // Nommer les variables attendues : un avertissement qui se contente de signaler
            // l'absence laisse chercher, et le symptôme (« impossible de se connecter ») ne
            // désigne pas sa cause.
            log.warn("Aucun compte d'administration amorcé : SONAGED_ADMIN_EMAIL et "
                    + "SONAGED_ADMIN_PASSWORD ne sont pas renseignés. L'API démarre, mais personne "
                    + "ne peut s'y connecter tant qu'aucun compte n'existe.");
            return;
        }

        switch (provisioning.createAdministratorIfAbsent(email, password)) {
            case CREATED -> log.info("Compte d'administration amorcé pour {}.", email);
            case ALREADY_PRESENT ->
                    log.info("Compte d'administration déjà présent ({}) : rien à faire.", email);
            case ROLE_MISSING -> log.error("Rôle d'administration introuvable : compte non créé. "
                    + "Les rôles de référence devraient être semés par le changelog 2.1.0.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
