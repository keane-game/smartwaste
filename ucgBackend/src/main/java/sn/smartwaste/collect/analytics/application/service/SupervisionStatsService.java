package sn.smartwaste.collect.analytics.application.service;

import java.util.UUID;

import sn.smartwaste.collect.analytics.application.dto.SupervisionStats;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.smartwaste.collect.shared.domain.model.DeletionStatus;
import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;
import sn.smartwaste.collect.platform.application.api.AlertStreamMetrics;
import sn.smartwaste.collect.waste.application.api.WasteReadModel;

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

    /**
     * Port publié par le contexte « Déchets ». Remplace l'injection de quatre repositories et la
     * navigation dans ses entités JPA (ADR-0013 §3) : ce service ne reçoit plus que des
     * projections autonomes.
     */
    private final WasteReadModel wasteReadModel;

    /** Port publié par le contexte « Plateforme » — nombre de flux SSE ouverts (ADR-0007). */
    private final AlertStreamMetrics alertStreamMetrics;

    private final Map<String, SoftDeleteRepository<?, ?>> softDeleteRepositories = new TreeMap<>();

    public SupervisionStatsService(WasteReadModel wasteReadModel,
                                   AlertStreamMetrics alertStreamMetrics,
                                   Map<String, SoftDeleteRepository<?, ?>> repositoriesByBeanName) {
        this.wasteReadModel = wasteReadModel;
        this.alertStreamMetrics = alertStreamMetrics;
        // La corbeille reste transverse : `SoftDeleteRepository` vit dans le shared kernel (module
        // ouvert) et l'injection se fait par type, sans dépendance vers les modules propriétaires.
        // Même convention de nommage que DeletionController : `depotoirRepository` -> `depotoir`.
        repositoriesByBeanName.forEach((beanName, repository) ->
                softDeleteRepositories.put(beanName.replaceFirst("(?i)repository$", "").toLowerCase(), repository));
    }

    @Transactional(readOnly = true)
    public SupervisionStats compute(Integer windowDays) {
        int window = (windowDays == null || windowDays <= 0) ? DEFAULT_WINDOW_DAYS : Math.min(windowDays, 365);

        List<WasteReadModel.ActiveAlert> alerts = wasteReadModel.activeAlerts();
        List<WasteReadModel.ActiveCollectionPoint> depotoirs = wasteReadModel.activeCollectionPoints();

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
                alertStreamMetrics.openStreamCount(),
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
    private List<SupervisionStats.DailyCount> alertsPerDay(List<WasteReadModel.ActiveAlert> alerts, LocalDate from, LocalDate to) {
        Map<LocalDate, Long> byDay = new LinkedHashMap<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            byDay.put(d, 0L);
        }
        for (WasteReadModel.ActiveAlert alert : alerts) {
            LocalDateTime created = alert.createdAt();
            if (created == null) { continue; }
            LocalDate day = created.toLocalDate();
            byDay.computeIfPresent(day, (k, v) -> v + 1);
        }
        return byDay.entrySet().stream()
                .map(e -> new SupervisionStats.DailyCount(e.getKey(), e.getValue()))
                .toList();
    }

    private Map<String, Long> alertsByCode(List<WasteReadModel.ActiveAlert> alerts) {
        return alerts.stream().collect(Collectors.groupingBy(
                a -> a.code() == null ? "NON_DEFINI" : a.code(),
                TreeMap::new, Collectors.counting()));
    }

    private Map<String, Long> depotoirsByType(List<WasteReadModel.ActiveCollectionPoint> depotoirs) {
        return depotoirs.stream().collect(Collectors.groupingBy(
                // Le type est résolu par le contexte propriétaire, dans SA transaction : la
                // contrainte LAZY de P1-2 ne remonte plus jusqu'ici.
                d -> d.typeName() == null ? "NON_DEFINI" : d.typeName(),
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
        wasteReadModel.activeCircuits().forEach(c -> increment(byCommune, c.communeId()));
        return byCommune;
    }

    private void increment(Map<String, Long> target, UUID communeId) {
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

    private long countSince(List<WasteReadModel.ActiveAlert> alerts, LocalDate since) {
        return alerts.stream()
                .map(WasteReadModel.ActiveAlert::createdAt)
                .filter(d -> d != null && !d.toLocalDate().isBefore(since))
                .count();
    }
}
