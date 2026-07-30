package sn.smartwaste.collect.iot.domain.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

/**
 * Capteur installé sur un point de collecte (ADR-0004).
 *
 * <p>Le matériel prévu par la spécification est un ESP8266 portant un HC-SR04 (distance → niveau de
 * remplissage) et un DHT11 (température et humidité), reliés en LoRa ou Wi-Fi.
 *
 * <p><b>La clé d'API n'est jamais stockée en clair</b>, seulement son empreinte SHA-256 : une base
 * exfiltrée ne doit pas permettre d'injecter de fausses mesures. Même raisonnement que pour les
 * jetons de rafraîchissement — et comme eux, la clé est un secret à forte entropie, ce qui rend un
 * hachage lent inutile.
 *
 * <p>{@code depotoirId} est une <b>référence par identifiant</b> vers le contexte « Déchets »
 * (ADR-0012) : aucune FK ne traverse la frontière.
 */
@Entity
@Table(name = "sensor")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Sensor extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "sensorId")
    UUID sensorId;

    /** Identifiant physique gravé sur le module (utile au terrain, pas à l'authentification). */
    @Column(name = "deviceCode", nullable = false, unique = true)
    String deviceCode;

    /** Empreinte SHA-256 de la clé d'API — jamais la clé elle-même. */
    @Column(name = "apiKeyHash", nullable = false, unique = true, length = 64)
    String apiKeyHash;

    /** Point de collecte instrumenté (contexte « Déchets », référencé par identifiant). */
    @Column(name = "depotoirId", nullable = false)
    Long depotoirId;

    /** Un capteur désactivé est refusé à l'ingestion sans être supprimé (remplacement, panne). */
    @Column(name = "active", nullable = false)
    boolean active = true;

    @Column(name = "lastSeenAt")
    Instant lastSeenAt;

    /**
     * Horodatage du signalement de silence en cours, {@code null} si le capteur est considere
     * comme vivant. Porte l'anti-repetition : sans lui, le planificateur signalerait a chaque
     * passage un capteur mort depuis une semaine.
     */
    @Column(name = "silenceReportedAt")
    Instant silenceReportedAt;
}
