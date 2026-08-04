package sn.smartwaste.collect.identity.application.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;

import sn.smartwaste.collect.identity.application.dto.User;
import sn.smartwaste.collect.identity.application.service.AuthService;
import sn.smartwaste.collect.identity.domain.repository.UserRepository;
import sn.smartwaste.collect.identity.domain.repository.ValidationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * L'inscription est atomique : sans code d'activation délivré, aucun compte ne subsiste.
 *
 * <p><b>Le défaut fermé ici.</b> {@code register} enregistrait le compte, <i>puis</i> publiait
 * l'événement d'activation, écouté <b>synchronement</b> par l'envoi de courriel. Serveur SMTP
 * absent — ce qui est le cas par défaut, {@code smtp4dev} n'étant pas démarré — l'exception
 * remontait jusqu'à l'appelant : <b>compte créé en base, HTTP 500 rendu au client</b>.
 *
 * <p>La conséquence est bien pire que le 500 lui-même. Le citoyen croit son inscription échouée,
 * recommence, et se voit répondre « Votre email est déjà utilisé ». Il ne peut ni entrer (le compte
 * n'est pas activé) ni se réinscrire, et aucun endpoint ne renvoie le code : <b>il est bloqué
 * définitivement</b>. Constaté en créant un vrai compte agent contre PostgreSQL.
 *
 * <p><b>Pourquoi tout annuler plutôt que d'ignorer l'échec d'envoi.</b> Un compte non activé dont
 * le code n'est jamais parvenu n'est pas un demi-succès : c'est une adresse rendue inutilisable.
 * En annulant, l'état reste propre et la deuxième tentative — cinq minutes plus tard, le courriel
 * rétabli — aboutit.
 *
 * <p><b>Pourquoi ce test démarre le contexte.</b> L'atomicité est portée par {@code @Transactional},
 * donc par le gestionnaire de transactions, pas par le code de la méthode. Un test unitaire
 * vérifierait que l'exception remonte — ce qu'elle faisait déjà — sans rien dire du rollback. C'est
 * la même leçon que la chaîne de filtres : ce qui se joue entre les mécanismes ne se voit qu'en les
 * exécutant ensemble.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:registeratomicity;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "sonaged.deletion.purge-cron=0 0 5 31 2 ?",
        "sonaged.alerts.stream.heartbeat-ms=3600000",
        "sonaged.collection.reminder.cron=0 0 5 31 2 ?",
        "sonaged.bootstrap.admin.enabled=false"
})
class RegisterAtomicityTest {

    private static final String EMAIL = "citoyen.pikine@example.sn";

    @Autowired private AuthService authService;
    @Autowired private sn.smartwaste.collect.identity.domain.repository.AuthorityRepository authorityRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ValidationRepository validationRepository;

    /** Serveur de courriel injoignable — l'état par défaut tant que smtp4dev n'est pas démarré. */
    @MockBean private JavaMailSender javaMailSender;

    /**
     * Seme le role par defaut de l'inscription.
     *
     * <p>Liquibase est desactive ici (ses changelogs utilisent des constructions que H2 refuse),
     * donc les roles de reference n'existent pas. Sans ce semis, {@code register} echouerait sur
     * « Role par defaut USER introuvable » AVANT d'atteindre l'envoi de courriel : le premier cas
     * passerait alors au vert sans rien prouver — l'absence de compte s'expliquant par un echec
     * anterieur, sans rapport avec ce qu'on teste.
     */
    @org.junit.jupiter.api.BeforeEach
    void seedDefaultRole() {
        // Les deux cas partagent la meme base en memoire : sans purge, le compte laisse par le
        // premier ferait echouer le second sur « email deja utilise ». Ce serait d'ailleurs le
        // symptome exact du defaut teste, mais applique au mauvais endroit.
        validationRepository.deleteAll();
        userRepository.deleteAll();

        if (authorityRepository.findByNameAndDeletionStatus("USER",
                sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE).isEmpty()) {
            var role = new sn.smartwaste.collect.identity.domain.model.AuthorityEntity();
            role.setName("USER");
            role.setDescription("Possibilite de voir");
            // Collection initialisee : `UserEntity.getAuthorities()` la parcourt hors session lors
            // du mapping, et un proxy non initialise leverait LazyInitializationException.
            role.setPermissions(new java.util.ArrayList<>());
            authorityRepository.save(role);
        }
    }

    private User candidate() {
        var user = new User();
        user.setUserEmail(EMAIL);
        user.setUserPassword("un-mot-de-passe");
        user.setUserFirstname("Citoyen");
        user.setUserLastname("Pikine");
        return user;
    }

    @Test
    @DisplayName("courriel injoignable : aucun compte ne subsiste, et la reinscription reste possible")
    void failedDeliveryLeavesNoAccountBehind() {
        doThrow(new MailSendException("serveur SMTP injoignable"))
                .when(javaMailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> authService.register(candidate()))
                .isInstanceOf(RuntimeException.class);

        // Le coeur du test. Sans rollback, cette adresse serait desormais « deja utilisee » pour
        // un compte que personne ne peut activer : le citoyen serait bloque sans recours.
        assertThat(userRepository.findByUserEmail(EMAIL)).isEmpty();
        assertThat(validationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("courriel delivre : le compte et son code d'activation sont enregistres")
    void successfulDeliveryPersistsBoth() {
        // La moitie qui compte autant : annuler trop large casserait l'inscription elle-meme.
        authService.register(candidate());

        assertThat(userRepository.findByUserEmail(EMAIL)).isPresent();
        assertThat(validationRepository.findAll()).hasSize(1);
    }
}
