package sn.smartwaste.collect.platform.application.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import sonaged.collecte.master.event.ActivationCodeIssued;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Vérifie que le contexte Communication construit l'e-mail d'activation à partir de la seule
 * charge utile de l'événement — sans dépendre des entités du contexte Identité (P1-7b).
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @DisplayName("construit le message à partir de l'événement et l'envoie")
    void sendActivationCode_buildsMessageFromEvent() {
        notificationService.sendActivationCode(
                new ActivationCodeIssued("awa@example.sn", "Diop", "042042"));

        ArgumentCaptor<SimpleMailMessage> sent = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(sent.capture());

        SimpleMailMessage message = sent.getValue();
        assertThat(message.getTo()).containsExactly("awa@example.sn");
        assertThat(message.getSubject()).isEqualTo("Votre code d'activation");
        // Le destinataire doit retrouver son nom et, surtout, un code exact :
        // une troncature ou un mauvais format rendrait l'activation impossible.
        assertThat(message.getText()).contains("Diop").contains("042042");
    }
}
