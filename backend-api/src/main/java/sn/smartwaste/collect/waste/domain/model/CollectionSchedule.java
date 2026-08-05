package sn.smartwaste.collect.waste.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
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

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

/**
 * Horaire de passage d'un circuit de collecte dans un quartier.
 *
 * <p><b>Pourquoi cette entité existe.</b> Le terrain décrit des circuits (« 66 circuits », « chaque
 * heure un camion passe ») et l'entité {@code CircuitCollect} porte déjà une {@code frequence} et
 * une {@code rotation} — mais en <b>texte libre</b>, inexploitable par une machine. Impossible d'en
 * déduire « le camion passe demain à 7 h dans ce quartier », donc impossible de prévenir qui que ce
 * soit. C'est ce chaînon qui manquait entre les données de collecte et l'habitant.
 *
 * <p>Le jour et l'heure sont structurés à dessein : c'est ce qui rend le rappel automatisable.
 */
@Entity
@Table(name = "collectionschedule",
       indexes = @Index(name = "idx_collectionschedule_day", columnList = "dayOfWeek"))
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CollectionSchedule extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "scheduleId")
    UUID scheduleId;

    /** Circuit qui effectue le passage (même contexte). */
    @Column(name = "circuitCollectId")
    Long circuitCollectId;

    /**
     * Quartier desservi — référence par identifiant vers le référentiel territorial (ADR-0012).
     * C'est la maille à laquelle l'habitant s'abonne : il connaît son quartier, pas son circuit.
     */
    @Column(name = "quartierId", nullable = false)
    UUID quartierId;

    @Enumerated(EnumType.STRING)
    @Column(name = "dayOfWeek", length = 12, nullable = false)
    DayOfWeek dayOfWeek;

    /** Heure de passage prévue. */
    @Column(name = "passageTime", nullable = false)
    LocalTime passageTime;

    /** Un horaire suspendu (travaux, saison) cesse de déclencher des rappels sans être supprimé. */
    @Column(name = "active", nullable = false)
    boolean active = true;
}
