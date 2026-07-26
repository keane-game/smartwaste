package sonaged.collecte.master.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import sonaged.collecte.master.event.AlertRaisedEvent;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Diffusion temps réel des alertes aux superviseurs abonnés (P2-1 / ADR-0007).
 *
 * <p>Maintient un registre d'{@link SseEmitter} par utilisateur authentifié. Un même utilisateur
 * peut avoir plusieurs flux (plusieurs onglets/appareils), d'où une liste par identifiant.
 *
 * <p><strong>Portée mono-instance assumée</strong> : le registre vit en mémoire. Avec plusieurs
 * instances derrière un load-balancer, un abonné ne reçoit que les alertes produites par
 * l'instance à laquelle il est connecté. L'ADR-0007 le prévoit explicitement et renvoie à un bus
 * partagé (Redis pub/sub) à traiter avec le multi-tenant (ADR-0008).
 */
@Service
public class AlertBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(AlertBroadcaster.class);

    /** Abonnés actifs, indexés par identifiant d'utilisateur (e-mail = sujet du JWT). */
    private final Map<String, List<SseEmitter>> subscribers = new ConcurrentHashMap<>();
    private final AtomicLong eventIds = new AtomicLong();
    private final long timeoutMillis;

    public AlertBroadcaster(
            @Value("${sonaged.alerts.stream.timeout-ms:1800000}") long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * Enregistre un nouvel abonné et lui renvoie son flux.
     *
     * <p>Le désabonnement est câblé sur les trois issues possibles (fin normale, expiration,
     * erreur réseau) : sans cela les émetteurs morts s'accumulent et fuient en mémoire.
     */
    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(timeoutMillis);
        subscribers.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError(e -> remove(userId, emitter));

        // Événement d'accueil : confirme l'ouverture au client et déclenche `onopen` côté navigateur.
        try {
            emitter.send(SseEmitter.event().name("connected").data("stream ouvert"));
        } catch (IOException e) {
            remove(userId, emitter);
        }
        log.debug("SSE : abonnement de {} ({} flux actifs)", userId, countEmitters());
        return emitter;
    }

    /**
     * Pousse l'alerte à tous les abonnés.
     *
     * <p>{@link TransactionalEventListener} en phase {@code AFTER_COMMIT} : on ne diffuse jamais
     * une alerte dont la transaction serait ensuite annulée. {@code fallbackExecution = true}
     * garantit la diffusion même quand la création a lieu hors transaction.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAlertRaised(AlertRaisedEvent event) {
        broadcast("alert", event);
    }

    /**
     * Ping périodique. Les proxies et load-balancers coupent volontiers une connexion inactive ;
     * un commentaire SSE régulier maintient le flux ouvert sans polluer les données applicatives.
     */
    @Scheduled(fixedDelayString = "${sonaged.alerts.stream.heartbeat-ms:30000}")
    public void heartbeat() {
        broadcast("heartbeat", System.currentTimeMillis());
    }

    private void broadcast(String eventName, Object payload) {
        if (subscribers.isEmpty()) {
            return;
        }
        String id = String.valueOf(eventIds.incrementAndGet());
        subscribers.forEach((userId, emitters) -> emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().id(id).name(eventName).data(payload));
            } catch (IOException | IllegalStateException e) {
                // Client parti ou flux déjà clos : on retire l'émetteur sans bruit.
                remove(userId, emitter);
            }
        }));
    }

    private void remove(String userId, SseEmitter emitter) {
        subscribers.computeIfPresent(userId, (k, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }

    /** Nombre de flux ouverts — exposé pour le tableau de bord de supervision (P2-5). */
    public int countEmitters() {
        return subscribers.values().stream().mapToInt(List::size).sum();
    }
}
