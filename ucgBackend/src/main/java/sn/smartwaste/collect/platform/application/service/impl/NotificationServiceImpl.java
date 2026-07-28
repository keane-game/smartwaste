package sn.smartwaste.collect.platform.application.service.impl;

import sn.smartwaste.collect.platform.application.service.NotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import sn.smartwaste.collect.shared.domain.event.ActivationCodeIssued;

/**
 * Envoi des e-mails sortants (contexte <b>Communication</b>).
 *
 * <p>Réagit à {@link ActivationCodeIssued} au lieu d'être appelé directement par le contexte
 * Identité &amp; Accès (P1-7b). Cette classe n'importe plus aucune entité de ce contexte :
 * elle ne dépend que de la charge utile de l'événement.
 *
 * <p>L'écoute est <b>synchrone</b> ({@code @EventListener}, et non
 * {@code @TransactionalEventListener}) afin de conserver le comportement d'origine : le code
 * était enregistré puis l'e-mail envoyé dans la foulée, une erreur d'envoi remontant à
 * l'appelant. Le découplage porte sur la structure, pas sur la sémantique.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final JavaMailSender javaMailSender;

    public NotificationServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    @EventListener
    public void sendActivationCode(ActivationCodeIssued event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@chillo.tech");
        message.setTo(event.recipientEmail());
        message.setSubject("Votre code d'activation");
        message.setText(String.format(
                "Bonjour %s, <br /> Votre code d'action est %s; A bientôt",
                event.recipientLastname(),
                event.code()
        ));

        javaMailSender.send(message);
        // Le code lui-même n'est jamais journalisé : c'est un secret d'activation.
        log.info("Code d'activation envoyé à {}", event.recipientEmail());
    }
}
