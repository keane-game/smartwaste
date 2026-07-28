package sn.smartwaste.collect.waste.application.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.shared.domain.event.AlertRaisedEvent;
import sn.smartwaste.collect.shared.domain.event.MeasurementRecorded;
import sn.smartwaste.collect.waste.domain.model.AlertCode;
import sn.smartwaste.collect.waste.domain.model.AlertEntity;
import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;
import sn.smartwaste.collect.waste.domain.repository.AlertRepository;
import sn.smartwaste.collect.waste.domain.repository.DepotoirRepository;

/**
 * Applique une mesure au point de collecte, et déclenche l'alerte quand le seuil est franchi.
 *
 * <p><b>C'est le maillon qui manquait au produit.</b> Jusqu'ici la chaîne s'arrêtait à une alerte
 * saisie à la main ; elle est désormais complète :
 * <pre>
 *   capteur ──POST /v1/measurements──▶ iot ──MeasurementRecorded──▶ waste (ici)
 *                                                                    │ seuil franchi
 *                                                                    ▼
 *                                              Alert ──AlertRaisedEvent──▶ platform (SSE)
 * </pre>
 * Le dernier maillon existait déjà : la diffusion temps réel écoute {@code AlertRaisedEvent} depuis
 * P2-1. Elle fonctionne sans modification, ce qui était exactement l'intention du découplage.
 *
 * <p><b>Pourquoi le seuil est évalué ici et non dans l'ingestion.</b> « 87 % » est une mesure ;
 * « 87 % justifie une alerte » est une règle métier déchets. La placer dans la chaîne IoT
 * obligerait à toucher l'ingestion pour changer un seuil.
 *
 * <p><b>Anti-répétition.</b> Un capteur émet en continu : sans garde, un bac resté plein
 * produirait une alerte par mesure, soit des centaines par jour. Une nouvelle alerte n'est levée
 * que sur le <b>franchissement</b> du seuil — passage sous le seuil, puis au-dessus. C'est le
 * défaut le plus courant de ce genre de moteur, et le plus pénible en exploitation.
 *
 * <p>Écouteur <b>synchrone</b> : l'ingestion et la mise à jour du niveau doivent partager la même
 * transaction. Une mesure enregistrée dont le niveau ne serait pas appliqué laisserait la carte
 * mentir jusqu'à la mesure suivante.
 */
@Component
public class FillLevelProjector {

    private static final Logger log = LoggerFactory.getLogger(FillLevelProjector.class);

    private final DepotoirRepository depotoirRepository;
    private final AlertRepository alertRepository;
    private final ApplicationEventPublisher eventPublisher;

    /** Seuil de déclenchement, en %. Global pour l'instant ; par type de dépotoir à terme. */
    private final int fillThresholdPercent;

    public FillLevelProjector(DepotoirRepository depotoirRepository,
                              AlertRepository alertRepository,
                              ApplicationEventPublisher eventPublisher,
                              @Value("${sonaged.alerting.fill-threshold-percent:80}") int fillThresholdPercent) {
        this.depotoirRepository = depotoirRepository;
        this.alertRepository = alertRepository;
        this.eventPublisher = eventPublisher;
        this.fillThresholdPercent = fillThresholdPercent;
    }

    @EventListener
    @Transactional
    public void on(MeasurementRecorded event) {
        if (event.fillLevelPercent() == null) {
            return; // mesure purement climatique (DHT11) : rien à projeter sur le remplissage
        }
        DepotoirEntity depotoir = depotoirRepository.findById(event.depotoirId()).orElse(null);
        if (depotoir == null) {
            // Capteur rattaché à un point supprimé : on ne perd pas la mesure (elle est déjà
            // persistée côté iot), mais il n'y a rien à mettre à jour.
            log.warn("Mesure recue pour un point de collecte inconnu ({}) — capteur {}",
                    event.depotoirId(), event.sensorId());
            return;
        }

        // Une mesure plus ancienne que le dernier état connu ne doit pas l'écraser : un capteur
        // hors ligne peut poster en différé, et le plus récent doit gagner.
        if (depotoir.getLastMeasuredAt() != null && event.measuredAt().isBefore(depotoir.getLastMeasuredAt())) {
            return;
        }

        Integer previous = depotoir.getFillLevelPercent();
        depotoir.setFillLevelPercent(event.fillLevelPercent());
        depotoir.setLastMeasuredAt(event.measuredAt());
        depotoirRepository.save(depotoir);

        if (crossesThreshold(previous, event.fillLevelPercent())) {
            raiseAlert(depotoir, event);
        }
    }

    /** Vrai uniquement au FRANCHISSEMENT : on était en dessous (ou inconnu), on passe au-dessus. */
    private boolean crossesThreshold(Integer previous, int current) {
        boolean wasBelow = previous == null || previous < fillThresholdPercent;
        return wasBelow && current >= fillThresholdPercent;
    }

    private void raiseAlert(DepotoirEntity depotoir, MeasurementRecorded event) {
        AlertEntity alert = new AlertEntity();
        alert.setObject("Point de collecte plein");
        alert.setMessage("Niveau de remplissage %d%% (seuil %d%%) mesure le %s."
                .formatted(event.fillLevelPercent(), fillThresholdPercent, event.measuredAt()));
        alert.setAddress(depotoir.getAddress());
        alert.setCode(AlertCode.WARNING);
        // ADR-0005 : l'alerte automatique est, elle, TOUJOURS rattachee a son point de collecte.
        alert.setDepotoirId(depotoir.getDepotoirId());
        alert.setCreatedDate(LocalDateTime.ofInstant(event.measuredAt(), ZoneId.systemDefault()));
        AlertEntity saved = alertRepository.save(alert);

        log.info("Seuil franchi sur le point {} : {}% >= {}% — alerte {} levee",
                depotoir.getDepotoirId(), event.fillLevelPercent(), fillThresholdPercent, saved.getAlertId());

        // Meme evenement que les alertes manuelles : la diffusion SSE fonctionne sans modification.
        eventPublisher.publishEvent(new AlertRaisedEvent(
                new AlertRaisedEvent.RaisedAlert(saved.getAlertId(), saved.getObject(),
                        saved.getMessage(), saved.getAddress(),
                        saved.getCode() == null ? null : saved.getCode().name(), null),
                AlertRaisedEvent.Source.THRESHOLD));
    }
}
