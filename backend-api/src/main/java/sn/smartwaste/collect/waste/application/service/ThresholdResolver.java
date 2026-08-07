package sn.smartwaste.collect.waste.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;
import sn.smartwaste.collect.waste.domain.repository.AlertThresholdRepository;

import java.util.UUID;

/**
 * Détermine les seuils applicables à un point de collecte.
 *
 * <p>Résolution en cascade : seuil du <b>type</b> s'il existe, sinon seuil <b>par défaut</b>
 * configuré en base, sinon la valeur de configuration historique. Cette dernière marche garantit
 * qu'une base neuve, sans aucun seuil saisi, continue d'alerter sur le remplissage — supprimer
 * l'alerte parce que personne n'a encore rempli un écran d'administration serait une régression
 * silencieuse.
 */
@Service
public class ThresholdResolver {

    private final AlertThresholdRepository thresholdRepository;
    private final int defaultFillThreshold;

    public ThresholdResolver(AlertThresholdRepository thresholdRepository,
                             @Value("${sonaged.alerting.fill-threshold-percent:80}") int defaultFillThreshold) {
        this.thresholdRepository = thresholdRepository;
        this.defaultFillThreshold = defaultFillThreshold;
    }

    @Transactional(readOnly = true)
    public EffectiveThresholds resolve(DepotoirEntity depotoir) {
        UUID typeId = depotoir.getTypeDepotoir() == null ? null : depotoir.getTypeDepotoir().getTypeDepotoirId();

        var configured = typeId == null
                ? thresholdRepository.findByTypeDepotoirIdIsNullAndActiveTrue()
                : thresholdRepository.findByTypeDepotoirIdAndActiveTrue(typeId)
                        .or(thresholdRepository::findByTypeDepotoirIdIsNullAndActiveTrue);

        return configured
                .map(t -> new EffectiveThresholds(
                        t.getFillLevelPercent() == null ? defaultFillThreshold : t.getFillLevelPercent(),
                        t.getTemperatureCelsius(),
                        t.getHumidityPercent()))
                .orElseGet(() -> new EffectiveThresholds(defaultFillThreshold, null, null));
    }

    /**
     * @param fillLevelPercent   toujours renseigné — le remplissage est le cœur du produit
     * @param temperatureCelsius {@code null} = grandeur non surveillée, ce qui n'est pas zéro
     * @param humidityPercent    idem
     */
    public record EffectiveThresholds(int fillLevelPercent,
                                      Double temperatureCelsius,
                                      Double humidityPercent) { }
}
