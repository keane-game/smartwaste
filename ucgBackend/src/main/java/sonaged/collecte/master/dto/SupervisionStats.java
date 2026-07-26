package sonaged.collecte.master.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Indicateurs avancés de supervision (P2-5).
 *
 * <p>Complète {@link DepartmentState}, qui ne fournit que des compteurs bruts, par des
 * indicateurs d'activité : volumétrie d'alertes dans le temps, répartitions, état de la
 * corbeille et nombre d'abonnés au flux temps réel.
 *
 * <p><strong>Absent volontairement : le taux de remplissage.</strong> Il suppose
 * {@code Depotoir.fillLevel}, alimenté par la chaîne d'ingestion IoT (ADR-0004, tâches P0-5/P0-6)
 * qui constitue un module <em>réservé</em>, non implémenté. Exposer un taux calculé sur des
 * données inexistantes produirait un indicateur faux — il sera ajouté avec l'ingestion.
 *
 * @param alertsPerDay        nombre d'alertes créées par jour, sur la fenêtre demandée
 * @param alertsByCode        répartition des alertes par code (INFO / WARNING / DANGER…)
 * @param depotoirsByType     répartition des points de collecte par type de dépotoir
 * @param circuitsByCommune   nombre de circuits (collecte + balayage) par identifiant de commune
 * @param totalAlerts         alertes actives (hors corbeille)
 * @param alertsLast7Days     alertes créées sur les 7 derniers jours
 * @param pendingDeletions    éléments en attente de purge, par ressource
 * @param liveSubscribers     flux SSE actuellement ouverts
 * @param windowDays          largeur de la fenêtre d'analyse, en jours
 */
public record SupervisionStats(
        List<DailyCount> alertsPerDay,
        Map<String, Long> alertsByCode,
        Map<String, Long> depotoirsByType,
        Map<String, Long> circuitsByCommune,
        long totalAlerts,
        long alertsLast7Days,
        Map<String, Long> pendingDeletions,
        int liveSubscribers,
        int windowDays
) {

    /** Compte journalier — les jours sans alerte sont présents avec {@code count = 0}. */
    public record DailyCount(LocalDate day, long count) { }
}
