package sn.smartwaste.collect.platform.application.service;

import java.util.List;
import java.util.UUID;

/**
 * Notifications poussées vers les appareils des citoyens (G2 du backlog).
 *
 * <p>Répond à l'exigence du mémoire §3.1.5.2 — « l'envoi et la réception d'alertes en temps réel »
 * vers l'application mobile — à laquelle le flux SSE ne répondait pas : il suppose une connexion
 * ouverte, donc une application au premier plan.
 */
public interface PushNotificationService {

    /**
     * Enregistre l'appareil d'un utilisateur, ou le rattache à lui s'il était déjà connu.
     *
     * <p>Une réinstallation redonne souvent le même jeton : le dupliquer ferait sonner l'appareil
     * deux fois.
     */
    void register(UUID userId, String token, String platform);

    /** Révoque un appareil — le citoyen s'est désabonné, ou a changé de téléphone. */
    void revoke(UUID userId, String token);

    /**
     * Notifie tous les appareils de ces destinataires.
     *
     * @return le nombre d'appareils effectivement joints — un échec n'interrompt jamais les suivants
     */
    int notify(List<UUID> userIds, String title, String body);
}
