package sn.smartwaste.collect.platform.infrastructure.notification;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import sn.smartwaste.collect.platform.application.service.AwarenessService;

/**
 * Déclenche la diffusion des messages de sensibilisation dont l'heure est venue (G3).
 *
 * <p><b>Pourquoi une classe distincte du service.</b> Poser {@code @Scheduled} sur le service
 * lui-même le faisait initialiser très tôt par le post-processeur d'ordonnancement — avant que les
 * repositories JPA ne soient enregistrés — et le contexte échouait sur
 * « No qualifying bean of type CollectionSubscriptionRepository ». Séparer le <i>quand</i> du
 * <i>quoi</i> évite ce couplage au cycle de démarrage, et suit ce que font déjà
 * {@code GeoJsonImportRunner} et {@code AdminBootstrapRunner}.
 */
@Component
public class AwarenessDispatchScheduler {

    private final AwarenessService awarenessService;

    public AwarenessDispatchScheduler(AwarenessService awarenessService) {
        this.awarenessService = awarenessService;
    }

    @Scheduled(cron = "${sonaged.awareness.dispatch-cron:0 */5 * * * *}")
    public void dispatchDue() {
        awarenessService.dispatchDue();
    }
}
