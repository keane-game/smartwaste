package sn.smartwaste.collect.waste.application.service.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import sn.smartwaste.collect.waste.domain.model.GeoDistance;
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
    /** Au-dela de cet age d'information, un point repasse devant malgre la geographie. */
    private final Duration maxStaleness;

    public CollectionRouteServiceImpl(DepotoirRepository depotoirRepository,
                                      Clock clock,
                                      @Value("${sonaged.alerting.fill-threshold-percent:80}") int fillThresholdPercent,
                                      @Value("${sonaged.routing.measurement-validity-hours:24}") long validityHours,
                                      @Value("${sonaged.routing.max-staleness-hours:72}") long maxStalenessHours) {
        this.depotoirRepository = depotoirRepository;
        this.clock = clock;
        this.fillThresholdPercent = fillThresholdPercent;
        this.measurementValidity = Duration.ofHours(validityHours);
        this.maxStaleness = Duration.ofHours(maxStalenessHours);
    }

    @Override
    public List<RouteStop> planForCommune(UUID communeId) {
        Instant now = Instant.now(clock);
        List<RouteStop> stops = depotoirRepository
                .findByCommuneIdAndDeletionStatus(communeId, DeletionStatus.ACTIVE).stream()
                .map(d -> toStop(d, now))
                .toList();

        // L'urgence decoupe la tournee en tranches ; la geographie n'ordonne QU'A L'INTERIEUR d'une
        // tranche. Un debordement a l'autre bout de la commune passe donc toujours avant un point
        // tiede qu'on a sous la main : une tournee optimisee qui laisse deborder n'a aucun sens.
        List<RouteStop> plan = new ArrayList<>();
        double[] depuis = null;
        for (StopPriority priority : StopPriority.values()) {
            List<RouteStop> tranche = stops.stream().filter(s -> s.priority() == priority)
                    .collect(Collectors.toCollection(ArrayList::new));
            if (tranche.isEmpty()) {
                continue;
            }
            depuis = chainByProximity(tranche, depuis, now, plan);
        }
        return List.copyOf(plan);
    }

    /**
     * Ordonne une tranche par proximite, en repartant du dernier point de la tranche precedente —
     * sinon la tournee se teleporterait a chaque changement d'urgence.
     *
     * <p>Deux garde-fous que le plus proche voisin, seul, ne donne pas :
     * <ul>
     *   <li><b>Anti-famine</b> : un point dont l'information depasse {@code maxStaleness} repasse
     *       devant. Sans cela un point isole peut etre repousse indefiniment — il y a toujours
     *       quelqu'un de plus pres — et c'est ce qui fait abandonner ce genre d'outil.</li>
     *   <li><b>Points sans position</b> : ils ne peuvent pas etre chaines, mais les exclure les
     *       rendrait invisibles. Ils ferment la tranche, dans l'ordre d'anciennete.</li>
     * </ul>
     *
     * @return la position du dernier point place, ou {@code depuis} si la tranche n'en donnait aucune
     */
    private double[] chainByProximity(List<RouteStop> tranche, double[] depuis, Instant now,
                                      List<RouteStop> plan) {
        List<RouteStop> delaisses = new ArrayList<>();
        List<RouteStop> sansPosition = new ArrayList<>();
        List<RouteStop> chainables = new ArrayList<>();
        for (RouteStop stop : tranche) {
            // Le rattrapage ne vise QUE les points reellement mesures, il y a longtemps. Un point
            // jamais mesure n'est pas delaisse : il est inconnu, ce que sa priorite dit deja.
            // Les confondre a coute cher — l'anciennete conventionnelle d'un point jamais mesure
            // (« depuis toujours ») depassant forcement l'age maximal, TOUTE la tranche basculait
            // dans le rattrapage et le chainage geographique ne s'executait jamais : sur les
            // 24 points de Mbao, la tournee sortait dans l'ordre du depot.
            if (stop.lastMeasuredAt() != null
                    && staleness(stop, now).compareTo(maxStaleness) > 0) {
                delaisses.add(stop);
            } else if (stop.latitude() == null || stop.longitude() == null) {
                sansPosition.add(stop);
            } else {
                chainables.add(stop);
            }
        }

        // Les delaisses d'abord, du plus ancien au moins ancien : c'est la dette qu'on rattrape.
        delaisses.sort(Comparator.comparing((RouteStop s) -> staleness(s, now)).reversed());
        plan.addAll(delaisses);
        double[] courant = depuis;
        for (RouteStop stop : delaisses) {
            if (stop.latitude() != null && stop.longitude() != null) {
                courant = new double[] { stop.latitude(), stop.longitude() };
            }
        }

        while (!chainables.isEmpty()) {
            RouteStop suivant = plusProche(chainables, courant, now);
            chainables.remove(suivant);
            plan.add(suivant);
            courant = new double[] { suivant.latitude(), suivant.longitude() };
        }

        sansPosition.sort(Comparator.comparing((RouteStop s) -> staleness(s, now)).reversed());
        plan.addAll(sansPosition);
        return courant;
    }

    /**
     * Le point le plus proche du precedent. Sans point de depart — premiere tranche de la tournee —
     * on part du plus ancien : a defaut de geographie, l'attente fait foi.
     */
    private RouteStop plusProche(List<RouteStop> candidats, double[] depuis, Instant now) {
        if (depuis == null) {
            return candidats.stream()
                    .max(Comparator.comparing(s -> staleness(s, now)))
                    .orElseThrow();
        }
        return candidats.stream()
                .min(Comparator.comparingDouble(s -> GeoDistance.metersBetween(
                        depuis[0], depuis[1], s.latitude(), s.longitude())))
                .orElseThrow();
    }

    /** Position du point de collecte, {@code null} tant qu'aucune geometrie ne lui est attachee. */
    private static double[] positionOf(DepotoirEntity depotoir) {
        var geometry = depotoir.getGeometry();
        if (geometry == null || geometry.getCoordinates() == null
                || geometry.getCoordinates().isEmpty()) {
            return null;
        }
        var first = geometry.getCoordinates().getFirst();
        try {
            return new double[] { Double.parseDouble(first.getLatitude()),
                                  Double.parseDouble(first.getLongitude()) };
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
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

        double[] position = positionOf(depotoir);
        return new RouteStop(depotoir.getDepotoirId(), depotoir.getAddress(),
                depotoir.getTypeDepotoir() == null ? null : depotoir.getTypeDepotoir().getName(),
                priority, fill, measuredAt, reason,
                position == null ? null : position[0], position == null ? null : position[1]);
    }

    /** Anciennete de l'information : sert a departager deux points de meme urgence. */
    private Duration staleness(RouteStop stop, Instant now) {
        return stop.lastMeasuredAt() == null
                // Jamais mesure = anciennete maximale : ce point attend depuis toujours.
                ? Duration.ofDays(3650)
                : Duration.between(stop.lastMeasuredAt(), now);
    }
}
