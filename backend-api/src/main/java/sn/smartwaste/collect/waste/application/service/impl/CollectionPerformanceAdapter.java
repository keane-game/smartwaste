package sn.smartwaste.collect.waste.application.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.shared.domain.model.DeletionStatus;
import sn.smartwaste.collect.waste.application.api.CollectionPerformance;
import sn.smartwaste.collect.waste.domain.model.AlertEntity;
import sn.smartwaste.collect.waste.domain.model.CollectionPassage;
import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;
import sn.smartwaste.collect.waste.domain.model.PassageOutcome;
import sn.smartwaste.collect.waste.domain.repository.AlertRepository;
import sn.smartwaste.collect.waste.domain.repository.CollectionPassageRepository;
import sn.smartwaste.collect.waste.domain.repository.DepotoirRepository;

/**
 * Calcule l'efficacité de la collecte sur une période (G5 du backlog).
 *
 * <p><b>Pourquoi l'agrégation vit ici.</b> Les alertes et les passages sont les entités de ce
 * contexte. Les faire remonter vers {@code analytics} pour qu'il les additionne traverserait une
 * frontière que {@code modules.verify()} refuse, et transporterait des milliers de lignes là où
 * quelques nombres suffisent.
 *
 * <p><b>Le plus grand nombre de points de collecte d'une commune est de 24</b> et la période
 * courante se compte en semaines : le volume rapatrié reste modeste, et le calcul en mémoire évite
 * autant de requêtes agrégées que d'indicateurs. Si le parc grandissait d'un ordre de grandeur, ce
 * sont ces trois lectures qu'il faudrait remplacer par des agrégats SQL — l'interface publiée, elle,
 * ne changerait pas.
 */
@Service
@Transactional(readOnly = true)
public class CollectionPerformanceAdapter implements CollectionPerformance {

    /** Au-delà, la liste cesse de désigner une action et devient un inventaire. */
    private static final int MAX_POINTS_CHRONIQUES = 10;

    private final DepotoirRepository depotoirRepository;
    private final AlertRepository alertRepository;
    private final CollectionPassageRepository passageRepository;

    public CollectionPerformanceAdapter(DepotoirRepository depotoirRepository,
                                        AlertRepository alertRepository,
                                        CollectionPassageRepository passageRepository) {
        this.depotoirRepository = depotoirRepository;
        this.alertRepository = alertRepository;
        this.passageRepository = passageRepository;
    }

    @Override
    public PerformanceReport reportFor(UUID communeId, Instant from, Instant to) {
        List<DepotoirEntity> points = communeId == null
                ? depotoirRepository.findByDeletionStatus(DeletionStatus.ACTIVE)
                : depotoirRepository.findByCommuneIdAndDeletionStatus(communeId,
                        DeletionStatus.ACTIVE);

        if (points.isEmpty()) {
            // Une commune peut n'avoir aucun point : 15 des 71 importes n'ont meme pas de commune.
            return new PerformanceReport(0, 0, null, 0, 0, 0, 0, List.of());
        }

        Map<UUID, String> adresses = points.stream().collect(Collectors.toMap(
                DepotoirEntity::getDepotoirId,
                d -> d.getAddress() == null ? "" : d.getAddress(),
                (a, b) -> a, LinkedHashMap::new));
        List<UUID> ids = List.copyOf(adresses.keySet());

        var alertes = alertRepository.findByDepotoirIdInAndCreatedDateBetween(
                ids, toLocal(from), toLocal(to));
        // Les alertes de maintenance mesurent la reactivite du technicien, pas celle du camion :
        // les melanger produirait une moyenne qui ne decrit ni l'une ni l'autre.
        var alertesCollecte = alertes.stream()
                .filter(a -> !SensorSilenceProjector.OBJET_SILENCE.equals(a.getObject()))
                .toList();

        var resolues = alertesCollecte.stream()
                .filter(a -> a.getResolvedAt() != null)
                .toList();
        // `null` et non zero : aucune resolution signifie « on ne sait pas », pas « instantane ».
        Double delaiMoyenHeures = resolues.isEmpty() ? null
                : resolues.stream()
                        .mapToLong(a -> Duration.between(a.getCreatedDate(), a.getResolvedAt())
                                .toMinutes())
                        .average().orElseThrow() / 60.0;

        var passages = passageRepository.findByDepotoirIdInAndOccurredAtBetween(ids, from, to);
        // Un point visite deux fois ne compte qu'une : c'est une couverture de territoire, pas un
        // compteur d'actes.
        long desservis = passages.stream().map(CollectionPassage::getDepotoirId).distinct().count();
        long collectes = distinctPointsWith(passages, PassageOutcome.COLLECTED);
        long inaccessibles = distinctPointsWith(passages, PassageOutcome.INACCESSIBLE);

        return new PerformanceReport(
                alertesCollecte.size(), resolues.size(), delaiMoyenHeures,
                points.size(), desservis, collectes, inaccessibles,
                chronicPoints(alertesCollecte, adresses));
    }

    private long distinctPointsWith(List<CollectionPassage> passages, PassageOutcome outcome) {
        return passages.stream()
                .filter(p -> p.getOutcome() == outcome)
                .map(CollectionPassage::getDepotoirId)
                .distinct().count();
    }

    /** Les points qui reviennent le plus souvent — le seul chiffre du rapport qui désigne une action. */
    private List<ProblemPoint> chronicPoints(List<AlertEntity> alertes, Map<UUID, String> adresses) {
        return alertes.stream()
                .collect(Collectors.groupingBy(AlertEntity::getDepotoirId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed()
                        // Départage stable : sans lui, deux points à égalité changeraient d'ordre
                        // d'un rapport à l'autre, et la liste paraîtrait bouger sans raison.
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(MAX_POINTS_CHRONIQUES)
                .map(e -> new ProblemPoint(e.getKey(),
                        adresses.getOrDefault(e.getKey(), ""), e.getValue()))
                .toList();
    }

    private static LocalDateTime toLocal(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
