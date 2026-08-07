package sn.smartwaste.collect.platform.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import sn.smartwaste.collect.platform.domain.model.AwarenessMessage;

/**
 * Messages de sensibilisation aux habitants (G3 du backlog).
 *
 * <p>Le mémoire les liste parmi les fonctionnalités du système (§3.1.5.2) et cite le manque de
 * sensibilisation comme <b>cause</b> du problème, pas seulement comme confort.
 */
public interface AwarenessService {

    /**
     * Programme un message vers un quartier, ou vers tous les abonnés si {@code quartierId} est
     * {@code null}.
     *
     * @throws org.springframework.web.server.ResponseStatusException si le plafond de fréquence du
     *         territoire est déjà atteint
     */
    default AwarenessMessage schedule(String title, String body, UUID quartierId, Instant scheduledAt) {
        return schedule(title, body, quartierId, scheduledAt, null);
    }

    /** Comme {@link #schedule(String, String, UUID, Instant)}, rattaché à une campagne. */
    AwarenessMessage schedule(String title, String body, UUID quartierId, Instant scheduledAt,
                              UUID campaignId);

    /** Diffuse les messages dont l'heure est venue. */
    void dispatchDue();

    /** Ce qu'un citoyen a reçu, du plus récent au plus ancien. */
    List<AwarenessMessage> receivedBy(UUID userId);
}
