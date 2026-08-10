package sn.smartwaste.collect.identity.application.service;

/**
 * Réinitialisation de mot de passe pour un compte oublié (ADR-0021, pont avant Keycloak).
 */
public interface PasswordResetService {

    /**
     * Déclenche l'envoi d'un jeton de réinitialisation si l'adresse correspond à un compte.
     *
     * <p><b>Ne révèle jamais si l'adresse est connue</b> : même comportement observable, que
     * l'e-mail existe ou non — un appelant anonyme ne doit pas pouvoir énumérer les comptes.
     */
    void requestReset(String email);

    /**
     * Consomme un jeton de réinitialisation et applique le nouveau mot de passe.
     *
     * <p>Révoque ensuite toutes les sessions ouvertes du compte — un mot de passe oublié réinitialisé
     * doit fermer tout accès obtenu entre-temps avec l'ancien.
     *
     * @throws sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException si le jeton
     *         est inconnu, déjà utilisé ou expiré
     */
    void confirmReset(String token, String newPassword);
}
