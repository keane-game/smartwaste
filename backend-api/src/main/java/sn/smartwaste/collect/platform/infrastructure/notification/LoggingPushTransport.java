package sn.smartwaste.collect.platform.infrastructure.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Transport par défaut : journalise au lieu d'envoyer (G2 du backlog).
 *
 * <p><b>Pourquoi il existe.</b> Aucun compte Firebase n'est configuré, et la clé Google du projet
 * est dans l'historique Git sans avoir été rotée (ADR-0002 §4-5) : brancher un vrai fournisseur
 * demande une décision d'exploitation, pas du code. En attendant, la chaîne complète — abonnement,
 * déclenchement, sélection des appareils, révocation des jetons morts — <b>fonctionne et
 * s'observe</b> ; seule la dernière poignée de main manque.
 *
 * <p>C'est délibérément un composant réel et non une pièce vide : le jour où un transport FCM sera
 * ajouté, il prendra sa place sans que rien d'autre ne bouge, et d'ici là le journal dit exactement
 * ce qui serait parti.
 */
@Component
@ConditionalOnMissingBean(ignored = LoggingPushTransport.class, value = PushTransport.class)
public class LoggingPushTransport implements PushTransport {

    private static final Logger log = LoggerFactory.getLogger(LoggingPushTransport.class);

    @Override
    public Result send(String deviceToken, String title, String body) {
        // Le jeton n'est pas journalisé en entier : c'est un identifiant d'appareil.
        log.info("Notification (aucun fournisseur configure) vers …{} : « {} » — {}",
                deviceToken.length() <= 6 ? deviceToken : deviceToken.substring(deviceToken.length() - 6),
                title, body);
        return Result.DELIVERED;
    }
}
