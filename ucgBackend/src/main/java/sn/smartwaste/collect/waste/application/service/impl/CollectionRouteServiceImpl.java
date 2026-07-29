package sn.smartwaste.collect.waste.application.service.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.shared.domain.model.DeletionStatus;
import sn.smartwaste.collect.waste.application.service.CollectionRouteService;
import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;
import sn.smartwaste.collect.waste.domain.repository.DepotoirRepository;

/**
 * Priorisation des tournées.
 *
 * <p>Trois règles portent tout le service, et chacune corrige une erreur qu'on commet naturellement
 * en écrivant ce tri.
 *
 * <p><b>1. Un point jamais mesuré n'est pas un point vide.</b> C'est l'erreur la plus coûteuse :
 * trier sur {@code fillLevel} en traitant {@code null} comme 0 relègue en fin de liste exactement
 * les points dont on ne sait rien — capteur en panne, jamais installé, hors réseau. Ils ne seraient
 * alors <b>jamais</b> collectés, et l'angle mort grandirait tout seul. Ils sont donc classés juste
 * après les débordements, en {@code ETAT_INCONNU}.
 *
 * <p><b>2. Une mesure ancienne vaut une absence de mesure.</b> Un niveau de 20 % daté d'il y a trois
 * jours ne dit rien de l'état d'aujourd'hui. Au-delà d'un délai de péremption, la mesure est traitée
 * comme inconnue plutôt que crue.
 *
 * <p><b>3. À urgence égale, le plus ancien passe devant.</b> Sans cela, un point à 85 % mesuré à
 * l'instant repasserait indéfiniment devant un point à 82 % qui attend depuis deux jours : le second
 * ne serait jamais servi. C'est de la famine, et c'est ce qui fait abandonner ce genre d'outil.
 */
@Service
@Transactional(readOnly = true)
public class CollectionRouteServiceImpl implements CollectionRouteService {

    private final DepotoirRepository depotoirRepository;
    private final Clock clock;
    private final int fillThresholdPercent;
    private final Duration measurementValidity;

    public CollectionRouteServiceImpl(DepotoirRepository depotoirRepository,
                                      Clock clock,
                                      @Value("${sonaged.alerting.fill-threshold-percent:80}") int fillThresholdPercent,
                                      @Value("${sonaged.routing.measurement-validity-hours:24}") long validityHours) {
        this.depotoirRepository = depotoirRepository;
        this.clock = clock;
        this.fillThresholdPercent = fillThresholdPercent;
        this.measurementValidity = Duration.ofHours(validityHours);
    }

    @Override
    public List<RouteStop> planForCommune(UUID communeId) {
        Instant now = Instant.now(clock);
        return depotoirRepository.findByCommuneIdAndDeletionStatus(communeId, DeletionStatus.ACTIVE).stream()
                .map(d -> toStop(d, now))
                // Tri : urgence d'abord, puis anciennete de l'information a urgence egale.
                .sorted(Comparator.comparing((RouteStop s) -> s.priority().ordinal())
                        .thenComparing(s -> staleness(s, now), Comparator.reverseOrder()))
                .toList();
    }

    private RouteStop toStop(DepotoirEntity depotoir, Instant now) {
        Integer fill = depotoir.getFillLevelPercent();
        Instant measuredAt = depotoir.getLastMeasuredAt();
        boolean stale = measuredAt == null || measuredAt.isBefore(now.minus(measurementValidity));

        StopPriority priority;
        String reason;
        if (fill != null && !stale && fill >= fillThresholdPercent) {
            priority = StopPriority.DEBORDEMENT;
            reason = "Niveau %d%% au-dessus du seuil de %d%%".formatted(fill, fillThresholdPercent);
        } else if (stale) {
            // Point jamais mesure OU mesure perimee : dans les deux cas, on ne SAIT PAS.
            priority = StopPriority.ETAT_INCONNU;
            reason = measuredAt == null
                    ? "Jamais mesure — etat inconnu, a verifier sur place"
                    : "Derniere mesure trop ancienne (%s) — etat inconnu".formatted(measuredAt);
        } else if (fill != null && fill >= fillThresholdPercent / 2) {
            priority = StopPriority.A_SURVEILLER;
            reason = "Niveau %d%%, se remplit".formatted(fill);
        } else {
            priority = StopPriority.RIEN_A_FAIRE;
            reason = "Niveau %d%%, vide recemment".formatted(fill == null ? 0 : fill);
        }

        return new RouteStop(depotoir.getDepotoirId(), depotoir.getAddress(),
                depotoir.getTypeDepotoir() == null ? null : depotoir.getTypeDepotoir().getName(),
                priority, fill, measuredAt, reason);
    }

    /** Anciennete de l'information : sert a departager deux points de meme urgence. */
    private Duration staleness(RouteStop stop, Instant now) {
        return stop.lastMeasuredAt() == null
                // Jamais mesure = anciennete maximale : ce point attend depuis toujours.
                ? Duration.ofDays(3650)
                : Duration.between(stop.lastMeasuredAt(), now);
    }
}
