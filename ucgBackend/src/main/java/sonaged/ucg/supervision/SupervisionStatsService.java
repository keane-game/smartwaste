package sonaged.ucg.supervision;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sonaged.collecte.master.enums.DeletionStatus;
import sonaged.collecte.master.model.AlertEntity;
import sonaged.collecte.master.model.DepotoirEntity;
import sonaged.collecte.master.repository.AlertRepository;
import sonaged.collecte.master.repository.CircuitBalayageRepository;
import sonaged.collecte.master.repository.CircuitCollectRepository;
import sonaged.collecte.master.repository.DepotoirRepository;
import sonaged.collecte.master.repository.SoftDeleteRepository;
import sonaged.collecte.master.service.notification.AlertBroadcaster;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Calcul des indicateurs avancés de supervision (P2-5).
 *
 * <p>Les agrégats sont réalisés en mémoire à partir des entités actives. Ce choix est
 * volontaire à ce stade : le volume actuel (quelques milliers de lignes) ne justifie pas
 * d'écrire des requêtes d'agrégation dédiées, et rester en Java évite de dupliquer la règle
 * « ne compter que les éléments ACTIVE » dans du JPQL. Si la volumétrie augmente — en
 * particulier quand l'ingestion IoT arrivera — ces méthodes devront passer en `@Query`
 * d'agrégation côté base.
 */
@Service
public class SupervisionStatsService {

    /** Fenêtre d'analyse par défaut, en jours. */
    private static final int DEFAULT_WINDOW_DAYS = 30;

    private final AlertRepository alertRepository;
    private final DepotoirRepository depotoirRepository;
    private final CircuitCollectRepository circuitCollectRepository;
    private final CircuitBalayageRepository circuitBalayageRepository;
    private final AlertBroadcaster alertBroadcaster;
    private final Map<String, SoftDeleteRepository<?, ?>> softDeleteRepositories = new TreeMap<>();

    public SupervisionStatsService(AlertRepository alertRepository,
                                   DepotoirRepository depotoirRepository,
                                   CircuitCollectRepository circuitCollectRepository,
                                   CircuitBalayageRepository circuitBalayageRepository,
                                   AlertBroadcaster alertBroadcaster,
                                   Map<String, SoftDeleteRepository<?, ?>> repositoriesByBeanName) {
        this.alertRepository = alertRepository;
        this.depotoirRepository = depotoirRepository;
        this.circuitCollectRepository = circuitCollectRepository;
        this.circuitBalayageRepository = circuitBalayageRepository;
        this.alertBroadcaster = alertBroadcaster;
        // Même convention de nommage que DeletionController : `depotoirRepository` -> `depotoir`.
        repositoriesByBeanName.forEach((beanName, repository) ->
                softDeleteRepositories.put(beanName.replaceFirst("(?i)repository$", "").toLowerCase(), repository));
    }

    @Transactional(readOnly = true)
    public SupervisionStats compute(Integer windowDays) {
        int window = (windowDays == null || windowDays <= 0) ? DEFAULT_WINDOW_DAYS : Math.min(windowDays, 365);

        List<AlertEntity> alerts = alertRepository.findByDeletionStatus(DeletionStatus.ACTIVE);
        List<DepotoirEntity> depotoirs = depotoirRepository.findByDeletionStatus(DeletionStatus.ACTIVE);

        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(window - 1L);

        return new SupervisionStats(
                alertsPerDay(alerts, from, today),
                alertsByCode(alerts),
                depotoirsByType(depotoirs),
                circuitsByCommune(),
                alerts.size(),
                countSince(alerts, today.minusDays(6)),
                pendingDeletions(),
                alertBroadcaster.countEmitters(),
                window
        );
    }

    /**
     * Série journalière du nombre d'alertes.
     *
     * <p>La série est pré-remplie à zéro sur toute la fenêtre : sans cela, les jours sans
     * alerte seraient absents et un graphique côté frontend les relierait en ligne droite,
     * masquant les creux d'activité.
     */
    private List<SupervisionStats.DailyCount> alertsPerDay(List<AlertEntity> alerts, LocalDate from, LocalDate to) {
        Map<LocalDate, Long> byDay = new LinkedHashMap<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            byDay.put(d, 0L);
        }
        for (AlertEntity alert : alerts) {
            LocalDateTime created = alert.getCreatedDate();
            if (created == null) { continue; }
            LocalDate day = created.toLocalDate();
            byDay.computeIfPresent(day, (k, v) -> v + 1);
        }
        return byDay.entrySet().stream()
                .map(e -> new SupervisionStats.DailyCount(e.getKey(), e.getValue()))
                .toList();
    }

    private Map<String, Long> alertsByCode(List<AlertEntity> alerts) {
        return alerts.stream().collect(Collectors.groupingBy(
                a -> a.getCode() == null ? "NON_DEFINI" : a.getCode().name(),
                TreeMap::new, Collectors.counting()));
    }

    private Map<String, Long> depotoirsByType(List<DepotoirEntity> depotoirs) {
        return depotoirs.stream().collect(Collectors.groupingBy(
                d -> {
                    // typeDepotoir est LAZY (P1-2) : l'accès reste dans la transaction en lecture.
                    var type = d.getTypeDepotoir();
                    return type == null || type.getName() == null ? "NON_DEFINI" : type.getName();
                },
                TreeMap::new, Collectors.counting()));
    }

    /**
     * Circuits par commune, tous types confondus (collecte + balayage).
     *
     * <p>Regroupe sur `communeId` — référence par identifiant depuis le découplage ADR-0012
     * (P1-7a) : il n'y a plus d'association objet vers Commune à naviguer.
     */
    private Map<String, Long> circuitsByCommune() {
        Map<String, Long> byCommune = new TreeMap<>();
        circuitCollectRepository.findByDeletionStatus(DeletionStatus.ACTIVE)
                .forEach(c -> increment(byCommune, c.getCommuneId()));
        circuitBalayageRepository.findByDeletionStatus(DeletionStatus.ACTIVE)
                .forEach(c -> increment(byCommune, c.getCommuneId()));
        return byCommune;
    }

    private void increment(Map<String, Long> target, Long communeId) {
        target.merge(communeId == null ? "NON_AFFECTE" : String.valueOf(communeId), 1L, Long::sum);
    }

    /** Nombre d'éléments en attente de purge, par ressource (corbeille non vide uniquement). */
    private Map<String, Long> pendingDeletions() {
        Map<String, Long> counts = new TreeMap<>();
        softDeleteRepositories.forEach((resource, repository) -> {
            long count = repository.countByDeletionStatus(DeletionStatus.PENDING_DELETION);
            if (count > 0) {
                counts.put(resource, count);
            }
        });
        return counts;
    }

    private long countSince(List<AlertEntity> alerts, LocalDate since) {
        return alerts.stream()
                .map(AlertEntity::getCreatedDate)
                .filter(d -> d != null && !d.toLocalDate().isBefore(since))
                .count();
    }
}
