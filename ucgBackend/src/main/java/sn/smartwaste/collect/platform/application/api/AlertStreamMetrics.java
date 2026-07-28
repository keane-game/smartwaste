package sn.smartwaste.collect.platform.application.api;

/**
 * Métriques de la diffusion temps réel des alertes (SSE).
 *
 * <p>Publié pour que « Supervision &amp; Analytique » puisse exposer le nombre de flux ouverts sans
 * dépendre d'{@code AlertBroadcaster}, qui est un détail d'infrastructure du contexte Plateforme
 * (registre d'{@code SseEmitter} en mémoire, portée mono-instance — cf. ADR-0007).
 */
public interface AlertStreamMetrics {

    /** Nombre de flux SSE actuellement ouverts sur cette instance. */
    int openStreamCount();
}
