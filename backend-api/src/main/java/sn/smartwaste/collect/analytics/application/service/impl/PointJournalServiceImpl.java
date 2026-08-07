package sn.smartwaste.collect.analytics.application.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.analytics.application.dto.PointJournal;
import sn.smartwaste.collect.analytics.application.service.PointJournalService;
import sn.smartwaste.collect.iot.application.api.IngestionMetrics;
import sn.smartwaste.collect.waste.application.api.WasteReadModel;

/**
 * Entrelace les faits que deux contextes détiennent sur un même point (G8 du backlog).
 *
 * <p>Le contexte IoT connaît les mesures, le contexte « Déchets » les alertes et les passages.
 * Chacun rend les siens ; l'histoire du point est leur entrelacement chronologique — et c'est tout
 * le travail de ce service.
 *
 * <p><b>Ce que cela ne fait pas.</b> Rien n'est écrit : le journal se lit dans des données déjà
 * produites par les lots 1 et 2. La coquille vide {@code HistoryEntity} n'est ni remplie ni
 * supprimée — son sort reste une question ouverte, qu'il n'y avait pas lieu de trancher pour
 * livrer ceci.
 */
@Service
@Transactional(readOnly = true)
public class PointJournalServiceImpl implements PointJournalService {

    private final WasteReadModel waste;
    private final IngestionMetrics iot;

    public PointJournalServiceImpl(WasteReadModel waste, IngestionMetrics iot) {
        this.waste = waste;
        this.iot = iot;
    }

    @Override
    public PointJournal journalFor(java.util.UUID depotoirId, Instant from, Instant to) {
        // Un journal vide se lirait « ce point n'a pas d'histoire », alors qu'il n'existe pas.
        if (!waste.collectionPointExists(depotoirId)) {
            return null;
        }

        var entries = new ArrayList<PointJournal.Entry>();

        for (var m : iot.measurementsFor(depotoirId, from, to)) {
            entries.add(new PointJournal.Entry(m.measuredAt(), "MESURE",
                    m.fillLevelPercent() == null ? "Mesure recue"
                            : "Niveau %d%%".formatted(m.fillLevelPercent()),
                    conditions(m)));
        }
        for (var e : waste.pointEvents(depotoirId, from, to)) {
            entries.add(new PointJournal.Entry(e.at(), e.kind(), e.label(), e.detail()));
        }

        entries.sort(Comparator.comparing(PointJournal.Entry::at));
        return new PointJournal(depotoirId, from, to, List.copyOf(entries));
    }

    /**
     * Température et humidité, <b>seulement si elles ont été mesurées</b>.
     *
     * <p>Un capteur peut ne transmettre que le remplissage. Afficher « 0 °C » serait une mesure
     * inventée, et un superviseur y lirait une anomalie thermique.
     */
    private String conditions(IngestionMetrics.RecordedMeasurement m) {
        var parts = new ArrayList<String>(2);
        if (m.temperatureCelsius() != null) {
            parts.add(String.format(Locale.FRANCE, "%.1f °C", m.temperatureCelsius()));
        }
        if (m.humidityPercent() != null) {
            parts.add(String.format(Locale.FRANCE, "%.0f %% d'humidite", m.humidityPercent()));
        }
        return parts.isEmpty() ? null : String.join(", ", parts);
    }
}
