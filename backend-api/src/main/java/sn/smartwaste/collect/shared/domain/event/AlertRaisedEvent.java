package sn.smartwaste.collect.shared.domain.event;

/**
 * Événement de domaine : une alerte vient d'être levée (P2-1 / ADR-0007).
 *
 * <p>Publié par le contexte « Déchets » via {@code ApplicationEventPublisher}, consommé par
 * l'{@code AlertBroadcaster} du contexte « Plateforme », qui pousse l'alerte aux superviseurs
 * abonnés en SSE.
 *
 * <p>Ce découplage par événement est délibéré (ADR-0004 §2, ADR-0013 §3) : le service métier ignore
 * qui écoute. Le jour où le moteur de seuils IoT (P0-6, module réservé) créera des alertes
 * automatiques, il publiera le même événement et la diffusion temps réel fonctionnera sans
 * modification. De même, passer à un broker (Kafka/MQTT) ne touchera que les abonnés.
 *
 * <p><b>Charge utile autonome — et pourquoi ce ne peut pas être le DTO {@code Alert}.</b>
 * L'événement transportait auparavant {@code waste.application.dto.Alert}. Or les contrats
 * d'événements vivent dans le shared kernel, module <i>ouvert</i> dont tout le système dépend :
 * cela faisait du contexte « Déchets » une dépendance implicite de tous les autres.
 * {@code modules.verify()} le signalait sans ambiguïté — « Module 'shared' depends on non-exposed
 * type … within module 'waste' ». Les champs sont donc recopiés ici, en types autonomes.
 *
 * <p>Le format transmis en SSE reste <b>inchangé</b> pour le frontend, qui lit
 * {@code alert.alertId}, {@code object}, {@code message}, {@code address}, {@code code} et
 * {@code image.url}. Deux champs disparaissent volontairement : {@code coordinate}, qu'aucun
 * abonné ne lit, et surtout {@code file} — un {@code MultipartFile}, c'est-à-dire un flux de
 * requête HTTP, qui n'a rien à faire dans une charge utile sérialisée et ne survit pas à la fin
 * de la requête.
 *
 * @param alert  l'alerte levée, sous forme autonome
 * @param source origine de l'alerte, pour permettre un filtrage côté client
 */
public record AlertRaisedEvent(RaisedAlert alert, Source source) {

    public enum Source {
        /** Alerte saisie par un utilisateur via l'API REST. */
        MANUAL,
        /** Alerte produite par le moteur de seuils (P0-6, réservé). */
        THRESHOLD
    }

    /**
     * Instantané de l'alerte au moment où elle est levée.
     *
     * @param code nom du code d'alerte, et non l'énumération : le shared kernel n'a pas à connaître
     *             le vocabulaire métier du contexte « Déchets », et la sérialisation Jackson
     *             produisait déjà exactement cette chaîne.
     */
    public record RaisedAlert(Long alertId,
                              String object,
                              String message,
                              String address,
                              String code,
                              ImageRef image) { }

    /** Référence à l'image jointe — l'URL suffit aux abonnés (vignette). */
    public record ImageRef(String url, String name) { }

    public static AlertRaisedEvent manual(RaisedAlert alert) {
        return new AlertRaisedEvent(alert, Source.MANUAL);
    }
}
