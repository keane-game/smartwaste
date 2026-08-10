package sn.smartwaste.collect.waste.application.service.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.shared.domain.event.AlertRaisedEvent;
import sn.smartwaste.collect.shared.domain.event.SensorBackOnline;
import sn.smartwaste.collect.shared.domain.event.SensorSilenceDetected;
import sn.smartwaste.collect.waste.domain.model.AlertCode;
import sn.smartwaste.collect.waste.domain.model.AlertEntity;
import sn.smartwaste.collect.waste.domain.repository.AlertRepository;
import sn.smartwaste.collect.waste.domain.repository.DepotoirRepository;

/**
 * Traduit le silence d'un capteur en alerte de maintenance (G7 du backlog).
 *
 * <p><b>Pourquoi une alerte distincte du débordement.</b> Un capteur muet et un bac plein sont deux
 * problèmes différents, adressés à deux personnes différentes : l'un appelle un technicien, l'autre
 * un camion. Les confondre brouillerait la tournée — un agent envoyé vider un bac dont on ignore
 * précisément l'état parce que le capteur est mort. L'objet de l'alerte les sépare
 * ({@value #OBJET_SILENCE}), et le point de collecte reste renseigné pour qu'on sache <i>où</i>
 * intervenir.
 *
 * <p><b>Pourquoi ici et pas dans la chaîne IoT.</b> « Ce capteur n'a rien dit depuis 10 h » est un
 * fait, que {@code iot} constate ; « cela justifie une alerte » est une règle métier déchets. Même
 * frontière que pour le franchissement de seuil.
 */
@Component
public class SensorSilenceProjector {

    private static final Logger log = LoggerFactory.getLogger(SensorSilenceProjector.class);

    static final String OBJET_SILENCE = "Capteur muet";

    private final AlertRepository alertRepository;
    private final DepotoirRepository depotoirRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public SensorSilenceProjector(AlertRepository alertRepository,
                                  DepotoirRepository depotoirRepository,
                                  ApplicationEventPublisher eventPublisher,
                                  Clock clock) {
        this.alertRepository = alertRepository;
        this.depotoirRepository = depotoirRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @EventListener
    @Transactional
    public void on(SensorSilenceDetected event) {
        var depotoir = depotoirRepository.findById(event.depotoirId());

        AlertEntity alert = new AlertEntity();
        alert.setObject(OBJET_SILENCE);
        alert.setMessage("Le capteur %s n'a plus emis depuis le %s. Le niveau de remplissage de ce "
                + "point n'est plus connu."
                .formatted(event.deviceCode(), event.silentSince()));
        alert.setAddress(depotoir.map(d -> d.getAddress()).orElse(null));
        // DANGER et non WARNING : un capteur mort ne se repare pas tout seul, et tant qu'il l'est
        // le point est un angle mort. Le debordement, lui, se resout au prochain passage.
        alert.setCode(AlertCode.DANGER);
        alert.setDepotoirId(event.depotoirId());
        // ADR-0020 : herite de la collectivite du point concerne — pas d'utilisateur authentifie
        // ici (declenche par un planificateur). Repli sur Pikine si le point a disparu entre-temps
        // (suppression concurrente) : plus vraisemblable que d'echouer une alerte de maintenance.
        alert.setOrganizationId(depotoir.map(d -> d.getOrganizationId())
                .orElse(sn.smartwaste.collect.tenant.application.api.CurrentTenantProvider.PIKINE_ORGANIZATION_ID));
        alert.setCreatedDate(LocalDateTime.ofInstant(event.detectedAt(), ZoneId.systemDefault()));
        AlertEntity saved = alertRepository.save(alert);

        log.warn("Alerte de maintenance levee pour le capteur {} (point {})",
                event.deviceCode(), event.depotoirId());

        // Meme evenement que les autres alertes : la diffusion SSE fonctionne sans modification.
        eventPublisher.publishEvent(new AlertRaisedEvent(
                new AlertRaisedEvent.RaisedAlert(saved.getAlertId(), saved.getObject(),
                        saved.getMessage(), saved.getAddress(),
                        saved.getCode() == null ? null : saved.getCode().name(), null),
                AlertRaisedEvent.Source.THRESHOLD));
    }

    @EventListener
    @Transactional
    public void on(SensorBackOnline event) {
        // Sans cette cloture, une alerte de silence resterait ouverte indefiniment et le tableau de
        // bord accumulerait des problemes deja resolus — jusqu'a ce qu'on cesse de le regarder.
        var ouvertes = alertRepository.findByDepotoirIdAndObjectAndResolvedAtIsNull(
                event.depotoirId(), OBJET_SILENCE);
        for (AlertEntity alert : ouvertes) {
            alert.setResolvedAt(LocalDateTime.ofInstant(event.backAt(), ZoneId.systemDefault()));
            alert.setResolvedBy("capteur " + event.deviceCode());
            alertRepository.save(alert);
        }
        if (!ouvertes.isEmpty()) {
            log.info("{} alerte(s) de silence refermee(s) pour le point {}",
                    ouvertes.size(), event.depotoirId());
        }
    }
}
