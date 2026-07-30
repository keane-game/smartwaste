package sn.smartwaste.collect.identity.application.api;

/**
 * Création du compte d'administration initial (ADR-0014 §2).
 *
 * <p><b>Pourquoi ce port existe.</b> L'amorçage est déclenché par le module {@code administration},
 * qui porte les opérations transverses d'exploitation — mais créer un compte, lui attribuer un rôle
 * et hacher un mot de passe relève de l'identité. Faire manipuler {@code UserRepository} et
 * {@code UserEntity} par {@code administration} traverserait la frontière de contexte que
 * l'ADR-0013 §3 interdit, et {@code modules.verify()} le refuse. Le module d'exploitation décide
 * <i>quand</i> amorcer ; l'identité seule sait <i>comment</i>.
 *
 * <p>Le contrat n'expose que des types autonomes : le jour où Keycloak devient le fournisseur
 * d'identité (ADR-0011), on réimplémente ce port sans toucher à l'appelant.
 */
public interface AdminAccountProvisioning {

    /**
     * Crée le compte d'administration s'il n'existe pas déjà.
     *
     * <p><b>Ne modifie jamais un compte existant</b> : ni son mot de passe, ni son rôle, ni son
     * activation. Un amorçage qui réécrirait le compte à chaque démarrage serait une porte dérobée
     * — poser une variable d'environnement suffirait à reprendre la main sur un compte dont le mot
     * de passe a été changé depuis.
     *
     * @param email       adresse du compte à amorcer
     * @param rawPassword mot de passe en clair, haché par l'implémentation
     * @return le résultat de la tentative, à consigner par l'appelant
     */
    Outcome createAdministratorIfAbsent(String email, String rawPassword);

    /** Ce qui s'est passé — l'appelant journalise, il ne réinterprète pas. */
    enum Outcome {
        /** Compte créé. */
        CREATED,
        /** Un compte portait déjà cette adresse : rien n'a été touché. */
        ALREADY_PRESENT,
        /** Le rôle d'administration n'est pas semé : aucun compte créé sans habilitation. */
        ROLE_MISSING
    }
}
