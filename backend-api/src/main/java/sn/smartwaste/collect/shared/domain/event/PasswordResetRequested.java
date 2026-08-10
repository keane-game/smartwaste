package sn.smartwaste.collect.shared.domain.event;

/**
 * Événement de domaine : une réinitialisation de mot de passe vient d'être demandée (ADR-0021).
 *
 * <p>Même raisonnement que {@link ActivationCodeIssued} : le contexte Identité &amp; Accès ne doit
 * pas appeler directement le contexte Communication (cycle que Spring Modulith refuse). La charge
 * utile est autonome — elle recopie les seules données nécessaires à l'envoi, jamais une entité JPA.
 *
 * @param recipientEmail    adresse de destination
 * @param recipientLastname nom affiché dans le message
 * @param token             jeton de réinitialisation en clair — n'existe qu'ici et dans l'e-mail,
 *                          jamais journalisé ni persisté (seule son empreinte l'est)
 */
public record PasswordResetRequested(
        String recipientEmail,
        String recipientLastname,
        String token
) { }
