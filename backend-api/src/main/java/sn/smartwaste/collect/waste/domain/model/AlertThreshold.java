package sn.smartwaste.collect.waste.domain.model;

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
 * Seuils de déclenchement d'alerte, par type de point de collecte.
 *
 * <p>Le mémoire confie à l'administrateur la configuration des seuils « de température, d'humidité
 * et de niveau de remplissage ». Jusqu'ici le remplissage était un <b>unique nombre global</b> dans
 * la configuration, et les deux autres n'existaient pas — alors que le capteur DHT11 prévu par la
 * spécification les mesure et que la chaîne les ingère depuis. La donnée était collectée puis
 * abandonnée.
 *
 * <p><b>Pourquoi par type.</b> Une caisse polybenne et un bac de rue ne se remplissent pas au même
 * rythme et ne débordent pas avec les mêmes conséquences. Un seuil unique oblige à choisir entre
 * alerter trop tôt sur les gros contenants et trop tard sur les petits.
 *
 * <p>Un seuil {@code null} signifie « ne pas surveiller cette grandeur » — c'est différent de zéro,
 * qui alerterait en permanence.
 */
@Entity
@Table(name = "alertthreshold")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AlertThreshold extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "thresholdId")
    UUID thresholdId;

    /**
     * Type de point de collecte visé. {@code null} = <b>seuil par défaut</b>, appliqué aux types
     * qui n'ont pas le leur.
     */
    @Column(name = "typeDepotoirId")
    UUID typeDepotoirId;

    /** Remplissage en %, au-delà duquel on alerte. {@code null} = non surveillé. */
    @Column(name = "fillLevelPercent")
    Integer fillLevelPercent;

    /** Température interne en °C. Au-delà : risque d'odeurs et de prolifération bactérienne. */
    @Column(name = "temperatureCelsius")
    Double temperatureCelsius;

    /** Humidité interne en %. Combinée à la chaleur, elle accélère la fermentation. */
    @Column(name = "humidityPercent")
    Double humidityPercent;

    @Column(name = "active", nullable = false)
    boolean active = true;
}
