package sonaged.collecte.master.event;

/**
 * Événement de domaine : un code d'activation vient d'être émis pour un compte (P1-7b).
 *
 * <p>Rompt le cycle <b>Identité &amp; Accès ↔ Communication</b> qui empêchait la migration
 * modulaire : {@code ValidationServiceImpl} appelait directement {@code NotificationService},
 * pendant que {@code NotificationServiceImpl} remontait vers les entités {@code Validation}
 * et {@code UserEntity}. Chaque contexte dépendait donc de l'autre — configuration que
 * Spring Modulith rejette, et qui interdit toute extraction ultérieure en microservice.
 *
 * <p>Conformément à l'ADR-0010 §2 (« communication inter-contexte par événements »), la charge
 * utile est <b>autonome</b> : elle recopie les seules données dont la communication a besoin,
 * plutôt que de transporter une entité JPA. Le contexte Communication n'importe donc plus
 * aucun type du contexte Identité, et le passage à un broker (ADR-0004) ne demandera que de
 * sérialiser ce record.
 *
 * @param recipientEmail    adresse de destination
 * @param recipientLastname nom affiché dans le message
 * @param code              code d'activation à six chiffres
 */
public record ActivationCodeIssued(
        String recipientEmail,
        String recipientLastname,
        String code
) { }
