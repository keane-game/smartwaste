package sn.smartwaste.collect.iot.application.api;

import java.time.Instant;
import java.util.List;

/**
 * Santé de la chaîne d'ingestion, à destination des rapports de supervision.
 *
 * <p>Publié parce qu'un tableau de bord qui ne dit rien de l'état du parc laisse croire que
 * l'absence d'alerte signifie l'absence de problème.
 */
public interface IngestionMetrics {

    /** Nombre de capteurs enrôlés et actifs. */
    long countActiveSensors();

    /** Nombre de points de collecte distincts effectivement instrumentés. */
    long countInstrumentedCollectionPoints();

    /** Mesures reçues depuis un instant donné. */
    long countMeasurementsSince(Instant since);

    /**
     * Capteurs actifs n'ayant rien transmis depuis le délai donné.
     *
     * <p><b>C'est l'indicateur le plus important du lot.</b> Un capteur muet ne produit aucune
     * alerte : sa panne ressemble donc, dans les chiffres, à un point de collecte qui va bien. Sans
     * cette liste, l'angle mort grandit sans que rien ne le signale.
     */
    List<SilentSensor> silentSensors(Instant since);

    /**
     * Mesures reçues pour un point de collecte, de la plus récente à la plus ancienne (G8).
     *
     * <p>Publié pour le journal d'un point : c'est la moitié de son histoire, l'autre venant du
     * contexte « Déchets » (alertes et passages). Aucune entité ne franchit la frontière.
     */
    List<RecordedMeasurement> measurementsFor(Long depotoirId, Instant from, Instant to);

    /** Une mesure, réduite à ce qu'un journal affiche. */
    record RecordedMeasurement(Instant measuredAt, Integer fillLevelPercent,
                               Double temperatureCelsius, Double humidityPercent,
                               String source) { }

    /** @param lastSeenAt {@code null} si le capteur n'a jamais émis depuis son enrôlement */
    record SilentSensor(String deviceCode, Long depotoirId, Instant lastSeenAt) { }
}
