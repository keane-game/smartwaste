package sn.smartwaste.collect.iot.domain.model;

/** Origine d'une mesure. */
public enum MeasurementSource {
    /** Transmise par un capteur via l'API d'ingestion. */
    IOT,
    /** Saisie par un agent (relevé manuel, capteur en panne). */
    MANUAL
}
