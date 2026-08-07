package sn.smartwaste.collect.analytics.application.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Indicateurs avancés de supervision (P2-5).
 *
 * <p>Complète {@link DepartmentState}, qui ne fournit que des compteurs bruts, par des
 * indicateurs d'activité : volumétrie d'alertes dans le temps, répartitions, état de la
 * corbeille et nombre d'abonnés au flux temps réel.
 *
 * <p><b>Quatre familles qui répondent à la même question sous des angles différents : le service
 * est-il réellement rendu ?</b> Les alertes disent ce que le système a détecté ;
 * {@code depotoirsByFillLevel} dit dans quel état est le terrain <i>maintenant</i> ;
 * {@code ingestion} dit ce que le système est <i>en état</i> de détecter ; {@code citizenReports}
 * dit ce que les habitants ont signalé eux-mêmes — donc, en creux, ce qui a échappé au reste.
 *
 * @param alertsPerDay          nombre d'alertes créées par jour, sur la fenêtre demandée
 * @param alertsByCode          répartition des alertes par code (INFO / WARNING / DANGER…)
 * @param depotoirsByType       répartition des points de collecte par type de dépotoir
 * @param depotoirsByFillLevel  répartition des points de collecte par tranche de remplissage
 * @param circuitsByCommune     nombre de circuits (collecte + balayage) par identifiant de commune
 * @param totalAlerts           alertes actives (hors corbeille)
 * @param alertsLast7Days       alertes créées sur les 7 derniers jours
 * @param pendingDeletions      éléments en attente de purge, par ressource
 * @param liveSubscribers       flux SSE actuellement ouverts
 * @param ingestion             santé de la chaîne de mesure
 * @param citizenReports        traitement des signalements citoyens
 * @param windowDays            largeur de la fenêtre d'analyse, en jours
 */
public record SupervisionStats(
        List<DailyCount> alertsPerDay,
        Map<String, Long> alertsByCode,
        Map<String, Long> depotoirsByType,
        Map<String, Long> depotoirsByFillLevel,
        Map<String, Long> circuitsByCommune,
        long totalAlerts,
        long alertsLast7Days,
        Map<String, Long> pendingDeletions,
        int liveSubscribers,
        IngestionHealth ingestion,
        CitizenReports citizenReports,
        int windowDays
) {

    /** Compte journalier — les jours sans alerte sont présents avec {@code count = 0}. */
    public record DailyCount(LocalDate day, long count) { }

    /**
     * Santé de la chaîne d'ingestion.
     *
     * <p><b>Pourquoi ce bloc existe.</b> Un capteur en panne ne produit aucune mesure, donc aucune
     * alerte : dans tous les autres indicateurs, sa défaillance est indiscernable d'un point de
     * collecte qui se porte bien. Sans {@code silentSensors}, l'angle mort grandit en silence et le
     * tableau de bord devient d'autant plus rassurant que le parc se dégrade.
     *
     * @param activeSensors                capteurs enrôlés et actifs
     * @param instrumentedCollectionPoints points de collecte distincts effectivement couverts
     * @param measurementsInWindow         mesures reçues sur la fenêtre d'analyse
     * @param silenceThresholdHours        durée au-delà de laquelle un capteur est dit muet
     * @param silentSensors                capteurs actifs muets, à aller voir sur le terrain
     */
    public record IngestionHealth(
            long activeSensors,
            long instrumentedCollectionPoints,
            long measurementsInWindow,
            int silenceThresholdHours,
            List<SilentSensor> silentSensors
    ) {
        /** @param lastSeenAt {@code null} si le capteur n'a jamais émis depuis son enrôlement */
        public record SilentSensor(String deviceCode, UUID depotoirId, Instant lastSeenAt) { }
    }

    /**
     * Traitement des signalements citoyens.
     *
     * @param byStatus                nombre de signalements par état, les états vides à zéro
     * @param medianResolutionMinutes délai médian dépôt → clôture, {@code null} si rien n'est clos
     * @param staleThresholdHours     durée au-delà de laquelle un signalement ouvert est dit oublié
     * @param openOlderThanThreshold  signalements ouverts depuis plus longtemps que ce seuil
     */
    public record CitizenReports(
            Map<String, Long> byStatus,
            Long medianResolutionMinutes,
            int staleThresholdHours,
            long openOlderThanThreshold
    ) { }
}
