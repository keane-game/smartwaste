package sn.smartwaste.collect.iot.domain.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

/**
 * Mesure transmise par un capteur — l'historique dont dépendent les tableaux de bord.
 *
 * <p>L'ADR-0004 écartait explicitement l'idée de ne garder que le dernier niveau sur le point de
 * collecte : sans historique, plus d'analyse de tendance ni d'optimisation de tournées. Le dernier
 * état <i>est</i> dénormalisé sur {@code Depotoir}, mais en plus de cette table, pas à sa place.
 *
 * <p>Les trois grandeurs sont facultatives : un capteur peut n'embarquer que l'ultrason, ou que le
 * DHT11. La spécification (cas d'usage administrateur du mémoire) demande des seuils sur les trois.
 *
 * <p>Table volumineuse par nature — index sur {@code (depotoirid, measuredat)}, l'axe de lecture
 * réel : « l'historique de CE point de collecte, du plus récent au plus ancien ».
 */
@Entity
@Table(name = "measurement",
       indexes = @Index(name = "idx_measurement_depotoir_measuredat",
                        columnList = "depotoirId, measuredAt"))
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Measurement {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "measurementId")
    UUID measurementId;

    @Column(name = "sensorId", nullable = false)
    UUID sensorId;

    @Column(name = "depotoirId", nullable = false)
    UUID depotoirId;

    /** Niveau de remplissage en pourcentage (0–100). */
    @Column(name = "fillLevelPercent")
    Integer fillLevelPercent;

    @Column(name = "temperatureCelsius")
    Double temperatureCelsius;

    @Column(name = "humidityPercent")
    Double humidityPercent;

    /** Horodatage de la MESURE, fourni par le capteur — distinct de la date de réception. */
    @Column(name = "measuredAt", nullable = false)
    Instant measuredAt;

    /** Date de réception côté serveur : un capteur hors ligne peut poster en différé. */
    @Column(name = "receivedAt", nullable = false)
    Instant receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 20, nullable = false)
    MeasurementSource source = MeasurementSource.IOT;
}
