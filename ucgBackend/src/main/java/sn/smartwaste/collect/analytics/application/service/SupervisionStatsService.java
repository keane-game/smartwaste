package sn.smartwaste.collect.analytics.application.service;

import java.util.UUID;

import sn.smartwaste.collect.analytics.application.dto.SupervisionStats;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.smartwaste.collect.shared.domain.model.DeletionStatus;
import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;
import sn.smartwaste.collect.iot.application.api.IngestionMetrics;
import sn.smartwaste.collect.platform.application.api.AlertStreamMetrics;
import sn.smartwaste.collect.platform.application.api.CitizenReportMetrics;
import sn.smartwaste.collect.waste.application.api.WasteReadModel;

import java.time.Duration;
import java.time.Instant;
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
 * particulier avec la montée en charge de l'ingestion IoT — ces méthodes devront passer en
 * `@Query` d'agrégation côté base. C'est déjà le cas de {@code countMeasurementsSince} et de
 * {@code countByDeletionStatus}, qui comptent en base : la table des mesures est la seule dont
 * le volume interdisait d'emblée un chargement complet.
 */
@Service
public class SupervisionStatsService {

    /** Fenêtre d'analyse par défaut, en jours. */
    private static final int DEFAULT_WINDOW_DAYS = 30;

    /**
     * Au-delà de 24 h sans transmission, un capteur est considéré muet.
     *
     * <p>Le pas d'émission attendu se compte en minutes (ADR-0004). Un jour entier de silence
     * laisse donc largement la place à une coupure réseau passagère : ce qui est signalé n'est plus
     * un aléa, c'est une installation à aller voir.
     */
    private static final int SILENCE_THRESHOLD_HOURS = 24;

    /**
     * Au-delà de 48 h, un signalement encore ouvert est compté comme oublié.
     *
     * <p>Deux jours ouvrés : le délai à partir duquel l'habitant qui a signalé un dépôt sauvage
     * cesse raisonnablement de croire que quelqu'un s'en occupe.
     */
    private static final int STALE_REPORT_THRESHOLD_HOURS = 48;

    /**
     * Tranches de remplissage, dans l'ordre d'affichage.
     *
     * <p>Bornes hautes exclusives, la dernière tranche capturant tout le reste. Le découpage suit
     * la lecture métier : en dessous de 50 % il n'y a rien à faire, 75 % est le seuil d'alerte par
     * défaut, et au-delà de 90 % le point déborde bientôt.
     */
    private static final int[] FILL_LEVEL_BOUNDS = { 50, 75, 90 };
    private static final String[] FILL_LEVEL_LABELS = { "0-49", "50-74", "75-89", "90-100" };

    /** Points de collecte sans capteur : leur niveau est inconnu, pas nul. */
    private static final String FILL_LEVEL_UNKNOWN = "NON_INSTRUMENTE";

    /**
     * Port publié par le contexte « Déchets ». Remplace l'injection de quatre repositories et la
     * navigation dans ses entités JPA (ADR-0013 §3) : ce service ne reçoit plus que des
     * projections autonomes.
     */
    private final WasteReadModel wasteReadModel;

    /** Port publié par le contexte « Plateforme » — nombre de flux SSE ouverts (ADR-0007). */
    private final AlertStreamMetrics alertStreamMetrics;

    /**
     * Port publié par le contexte « Ingestion IoT ». La supervision ne voit ni capteur ni mesure —
     * seulement des agrégats : le module est le premier candidat à l'extraction en microservice
     * (ADR-0013), et rien ici ne doit rendre cette extraction plus coûteuse.
     */
    private final IngestionMetrics ingestionMetrics;

    /** Port publié par le contexte « Plateforme » — traitement des signalements citoyens. */
    private final CitizenReportMetrics citizenReportMetrics;

    private final Map<String, SoftDeleteRepository<?, ?>> softDeleteRepositories = new TreeMap<>();

    public SupervisionStatsService(WasteReadModel wasteReadModel,
                                   AlertStreamMetrics alertStreamMetrics,
                                   IngestionMetrics ingestionMetrics,
                                   CitizenReportMetrics citizenReportMetrics,
                                   Map<String, SoftDeleteRepository<?, ?>> repositoriesByBeanName) {
        this.wasteReadModel = wasteReadModel;
        this.alertStreamMetrics = alertStreamMetrics;
        this.ingestionMetrics = ingestionMetrics;
        this.citizenReportMetrics = citizenReportMetrics;
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
                depotoirsByFillLevel(depotoirs),
                circuitsByCommune(),
                alerts.size(),
                countSince(alerts, today.minusDays(6)),
                pendingDeletions(),
                alertStreamMetrics.openStreamCount(),
                ingestionHealth(window),
                citizenReports(),
                window
        );
    }

    /**
     * Santé de la chaîne de mesure.
     *
     * <p>Les mesures sont comptées sur la <b>fenêtre demandée</b>, comme les alertes : les deux
     * séries se lisent l'une contre l'autre, et un dénombrement sur une autre période rendrait la
     * comparaison trompeuse. Le silence, lui, se juge sur un délai fixe — ce n'est pas une
     * volumétrie mais un état du matériel, indépendant de la largeur de la fenêtre.
     */
    private SupervisionStats.IngestionHealth ingestionHealth(int windowDays) {
        Instant windowStart = Instant.now().minus(Duration.ofDays(windowDays));
        Instant silenceCutoff = Instant.now().minus(Duration.ofHours(SILENCE_THRESHOLD_HOURS));

        List<SupervisionStats.IngestionHealth.SilentSensor> silent = ingestionMetrics.silentSensors(silenceCutoff)
                .stream()
                // Recopié plutôt que republié tel quel : ce type est le contrat HTTP de la
                // supervision. Réexposer le record du port ferait d'un renommage interne à
                // l'ingestion une rupture d'API, sans que rien ne le signale.
                .map(s -> new SupervisionStats.IngestionHealth.SilentSensor(
                        s.deviceCode(), s.depotoirId(), s.lastSeenAt()))
                .toList();

        return new SupervisionStats.IngestionHealth(
                ingestionMetrics.countActiveSensors(),
                ingestionMetrics.countInstrumentedCollectionPoints(),
                ingestionMetrics.countMeasurementsSince(windowStart),
                SILENCE_THRESHOLD_HOURS,
                silent
        );
    }

    /** Traitement des signalements citoyens. */
    private SupervisionStats.CitizenReports citizenReports() {
        Long medianMinutes = citizenReportMetrics.medianResolutionTime()
                .map(Duration::toMinutes)
                .orElse(null);

        return new SupervisionStats.CitizenReports(
                citizenReportMetrics.countByStatus(),
                medianMinutes,
                STALE_REPORT_THRESHOLD_HOURS,
                citizenReportMetrics.countOpenOlderThan(Duration.ofHours(STALE_REPORT_THRESHOLD_HOURS))
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
     * Répartition des points de collecte par tranche de remplissage.
     *
     * <p>C'est l'indicateur que le mémoire attend en premier — l'état du terrain à l'instant t —
     * et il n'était pas calculable tant que rien n'alimentait {@code fillLevelPercent}. Il l'est
     * depuis que l'ingestion projette les mesures sur le point de collecte.
     *
     * <p><b>Les points non instrumentés sont comptés à part, jamais à zéro.</b> Les ranger dans la
     * tranche « 0-49 » ferait passer un parc sans capteurs pour un parc vide — exactement
     * l'illusion que ce tableau de bord doit empêcher. Toutes les tranches sont pré-remplies à
     * zéro pour la même raison qu'{@link #alertsPerDay} : une tranche absente se lit comme une
     * donnée manquante, pas comme un résultat.
     */
    private Map<String, Long> depotoirsByFillLevel(List<WasteReadModel.ActiveCollectionPoint> depotoirs) {
        Map<String, Long> byBucket = new LinkedHashMap<>();
        for (String label : FILL_LEVEL_LABELS) {
            byBucket.put(label, 0L);
        }
        byBucket.put(FILL_LEVEL_UNKNOWN, 0L);

        for (WasteReadModel.ActiveCollectionPoint depotoir : depotoirs) {
            byBucket.merge(fillLevelBucket(depotoir.fillLevelPercent()), 1L, Long::sum);
        }
        return byBucket;
    }

    private String fillLevelBucket(Integer fillLevelPercent) {
        if (fillLevelPercent == null) {
            return FILL_LEVEL_UNKNOWN;
        }
        for (int i = 0; i < FILL_LEVEL_BOUNDS.length; i++) {
            if (fillLevelPercent < FILL_LEVEL_BOUNDS[i]) {
                return FILL_LEVEL_LABELS[i];
            }
        }
        return FILL_LEVEL_LABELS[FILL_LEVEL_LABELS.length - 1];
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
