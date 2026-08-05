package sn.smartwaste.collect.platform.infrastructure.notification;

/**
 * Le canal par lequel une notification quitte le système (G2 du backlog).
 *
 * <p><b>Pourquoi une interface.</b> Le fournisseur — FCM aujourd'hui, autre chose demain — est un
 * détail d'infrastructure. Le métier a besoin de savoir qu'un message est parti, et surtout de
 * distinguer <b>« ce jeton n'existe plus »</b> de <b>« le réseau est tombé »</b> : le premier
 * justifie une révocation, le second surtout pas.
 */
public interface PushTransport {

    /** @return ce que le fournisseur a répondu, jamais {@code null} */
    Result send(String deviceToken, String title, String body);

    enum Result {
        /** Le fournisseur a accepté le message. */
        DELIVERED,
        /** Le fournisseur déclare ce jeton inconnu ou périmé : l'appareil a disparu. */
        INVALID_TOKEN,
        /** Échec temporaire — réseau, quota, panne. Le jeton reste valide. */
        FAILED
    }
}
