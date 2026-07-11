package sonaged.collecte.master.service.impl;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.model.Validation;
import sonaged.collecte.master.service.NotificationService;

@AllArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService {

    JavaMailSender javaMailSender;
    public void sendCodeOfValidation(Validation validation) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@chillo.tech");
        message.setTo(validation.getUser ().getUserEmail ());
        message.setSubject("Votre code d'activation");

        String texte = String.format(
                "Bonjour %s, <br /> Votre code d'action est %s; A bientôt",
                validation.getUser ().getUserLastname (),
                validation.getCode()
        );
        message.setText(texte);

        javaMailSender.send(message);
    }
}
