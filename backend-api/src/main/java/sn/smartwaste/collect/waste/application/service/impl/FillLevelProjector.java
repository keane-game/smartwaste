package sn.smartwaste.collect.waste.application.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import sn.smartwaste.collect.waste.application.service.ThresholdResolver;
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

    /** Seuils applicables au point mesuré : par type, avec repli sur le seuil par defaut. */
    private final ThresholdResolver thresholdResolver;

    public FillLevelProjector(DepotoirRepository depotoirRepository,
                              AlertRepository alertRepository,
                              ApplicationEventPublisher eventPublisher,
                              ThresholdResolver thresholdResolver) {
        this.depotoirRepository = depotoirRepository;
        this.alertRepository = alertRepository;
        this.eventPublisher = eventPublisher;
        this.thresholdResolver = thresholdResolver;
    }

    @EventListener
    @Transactional
    public void on(MeasurementRecorded event) {
        DepotoirEntity depotoir = depotoirRepository.findById(event.depotoirId()).orElse(null);
        if (depotoir == null) {
            // Capteur rattaché à un point supprimé : la mesure est déjà persistée côté iot, mais
            // il n'y a rien à mettre à jour.
            log.warn("Mesure recue pour un point de collecte inconnu ({}) — capteur {}",
                    event.depotoirId(), event.sensorId());
            return;
        }

        // Une mesure plus ancienne que le dernier état connu ne doit pas l'écraser : un capteur
        // hors ligne peut poster en différé, et le plus récent doit gagner.
        if (depotoir.getLastMeasuredAt() != null && event.measuredAt().isBefore(depotoir.getLastMeasuredAt())) {
            return;
        }

        var thresholds = thresholdResolver.resolve(depotoir);
        Integer previousFill = depotoir.getFillLevelPercent();
        Double previousTemperature = depotoir.getLastTemperatureCelsius();
        Double previousHumidity = depotoir.getLastHumidityPercent();

        boolean changed = false;
        if (event.fillLevelPercent() != null) {
            depotoir.setFillLevelPercent(event.fillLevelPercent());
            changed = true;
        }
        if (event.temperatureCelsius() != null) {
            depotoir.setLastTemperatureCelsius(event.temperatureCelsius());
            changed = true;
        }
        if (event.humidityPercent() != null) {
            depotoir.setLastHumidityPercent(event.humidityPercent());
            changed = true;
        }
        if (!changed) {
            return; // mesure vide : rien à projeter
        }
        depotoir.setLastMeasuredAt(event.measuredAt());
        depotoirRepository.save(depotoir);

        // Les trois grandeurs sont evaluees INDEPENDAMMENT : un bac peut deborder ET fermenter,
        // ce sont deux problemes distincts, pour deux interventions distinctes.
        if (crosses(previousFill, event.fillLevelPercent(), (double) thresholds.fillLevelPercent())) {
            raise(depotoir, AlertCode.WARNING, "Point de collecte plein",
                    "Niveau de remplissage %d%% (seuil %d%%) mesure le %s."
                            .formatted(event.fillLevelPercent(), thresholds.fillLevelPercent(), event.measuredAt()),
                    event.measuredAt());
        }
        if (crosses(previousTemperature, event.temperatureCelsius(), thresholds.temperatureCelsius())) {
            // Le capteur DHT11 est prevu par la specification precisement pour ca : au-dela d'un
            // certain seuil, odeurs et prolifération bacterienne.
            raise(depotoir, AlertCode.DANGER, "Temperature anormale",
                    "Temperature interne %.1f°C (seuil %.1f°C) mesuree le %s."
                            .formatted(event.temperatureCelsius(), thresholds.temperatureCelsius(), event.measuredAt()),
                    event.measuredAt());
        }
        if (crosses(previousHumidity, event.humidityPercent(), thresholds.humidityPercent())) {
            raise(depotoir, AlertCode.DANGER, "Humidite anormale",
                    "Humidite interne %.1f%% (seuil %.1f%%) mesuree le %s."
                            .formatted(event.humidityPercent(), thresholds.humidityPercent(), event.measuredAt()),
                    event.measuredAt());
        }
    }

    /**
     * Vrai uniquement au FRANCHISSEMENT : on était en dessous (ou inconnu), on passe au-dessus.
     *
     * <p>Un seuil {@code null} ne déclenche jamais : la grandeur n'est pas surveillée, ce qui est
     * différent d'un seuil à zéro qui alerterait en permanence.
     */
    private boolean crosses(Number previous, Number current, Double threshold) {
        if (threshold == null || current == null) {
            return false;
        }
        boolean wasBelow = previous == null || previous.doubleValue() < threshold;
        return wasBelow && current.doubleValue() >= threshold;
    }

    private void raise(DepotoirEntity depotoir, AlertCode code, String object, String message,
                       Instant measuredAt) {
        AlertEntity alert = new AlertEntity();
        alert.setObject(object);
        alert.setMessage(message);
        alert.setAddress(depotoir.getAddress());
        alert.setCode(code);
        // ADR-0005 : l'alerte automatique est, elle, TOUJOURS rattachee a son point de collecte.
        alert.setDepotoirId(depotoir.getDepotoirId());
        // ADR-0020 : une alerte automatique herite de la collectivite du point qu'elle concerne —
        // il n'y a pas d'utilisateur authentifie ici (declenchee par une mesure IoT), donc pas de
        // CurrentTenantProvider a interroger.
        alert.setOrganizationId(depotoir.getOrganizationId());
        alert.setCreatedDate(LocalDateTime.ofInstant(measuredAt, ZoneId.systemDefault()));
        AlertEntity saved = alertRepository.save(alert);

        log.info("Seuil franchi sur le point {} : {} — alerte {} levee",
                depotoir.getDepotoirId(), object, saved.getAlertId());

        // Meme evenement que les alertes manuelles : la diffusion SSE fonctionne sans modification.
        eventPublisher.publishEvent(new AlertRaisedEvent(
                new AlertRaisedEvent.RaisedAlert(saved.getAlertId(), saved.getObject(),
                        saved.getMessage(), saved.getAddress(),
                        saved.getCode() == null ? null : saved.getCode().name(), null),
                AlertRaisedEvent.Source.THRESHOLD));
    }
}
