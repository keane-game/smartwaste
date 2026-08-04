package sn.smartwaste.collect.analytics.application.dto;

import java.time.Instant;
import java.util.List;

/**
 * L'histoire d'un point de collecte (G8 du backlog).
 *
 * <p>Entrelace ce que deux contextes savent : les mesures reçues de ses capteurs, et les alertes et
 * passages qui l'ont concerné. Lus séparément, ces faits ne racontent rien ; entrelacés, ils disent
 * « le bac s'est rempli, l'alerte est partie, l'agent est passé ».
 */
public record PointJournal(Long depotoirId, Instant from, Instant to, List<Entry> entries) {

    /**
     * Un fait daté.
     *
     * @param kind {@code MESURE}, {@code ALERTE_LEVEE}, {@code ALERTE_RESOLUE}, {@code COLLECTE}
     *             ou {@code INACCESSIBLE}
     */
    public record Entry(Instant at, String kind, String label, String detail) { }
}
