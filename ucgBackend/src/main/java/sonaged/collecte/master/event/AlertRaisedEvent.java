package sonaged.collecte.master.event;

import sonaged.collecte.master.dto.Alert;

/**
 * Événement de domaine : une alerte vient d'être levée (P2-1 / ADR-0007).
 *
 * <p>Publié par le contexte « Alertes » via {@code ApplicationEventPublisher}, consommé par
 * l'{@code AlertBroadcaster} qui pousse l'alerte aux superviseurs abonnés en SSE.
 *
 * <p>Ce découplage par événement est délibéré (ADR-0004 §2, ADR-0010 §2) : le service métier
 * ignore qui écoute. Le jour où le moteur de seuils IoT (P0-6, module réservé) créera des
 * alertes automatiques, il publiera le même événement et la diffusion temps réel fonctionnera
 * sans modification. De même, passer à un broker (Kafka/MQTT) ne touchera que les abonnés.
 *
 * @param alert  l'alerte levée, sous sa forme DTO (jamais l'entité JPA)
 * @param source origine de l'alerte, pour permettre un filtrage côté client
 */
public record AlertRaisedEvent(Alert alert, Source source) {

    public enum Source {
        /** Alerte saisie par un utilisateur via l'API REST. */
        MANUAL,
        /** Alerte produite par le moteur de seuils (P0-6, réservé). */
        THRESHOLD
    }

    public static AlertRaisedEvent manual(Alert alert) {
        return new AlertRaisedEvent(alert, Source.MANUAL);
    }
}
