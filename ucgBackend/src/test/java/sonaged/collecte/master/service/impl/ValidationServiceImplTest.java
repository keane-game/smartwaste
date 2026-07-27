package sonaged.collecte.master.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sonaged.collecte.master.event.ActivationCodeIssued;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.model.UserEntity;
import sonaged.collecte.master.model.Validation;
import sonaged.collecte.master.repository.ValidationRepository;

import java.util.Optional;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Vérifie le découplage Identité ↔ Communication introduit en P1-7b : l'émission d'un code
 * d'activation doit **publier un événement** et non appeler le service de notification.
 */
@ExtendWith(MockitoExtension.class)
class ValidationServiceImplTest {

    private static final Pattern SIX_DIGITS = Pattern.compile("\\d{6}");

    @Mock
    private ValidationRepository validationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ValidationServiceImpl validationService;

    private static UserEntity user(String email, String lastname) {
        UserEntity user = new UserEntity();
        user.setUserEmail(email);
        user.setUserLastname(lastname);
        return user;
    }

    @Test
    @DisplayName("enregistre la validation puis publie ActivationCodeIssued avec la charge utile attendue")
    void registerUserCode_publishesEventWithRecipientAndCode() {
        validationService.registerUserCode(user("awa@example.sn", "Diop"));

        ArgumentCaptor<Validation> saved = ArgumentCaptor.forClass(Validation.class);
        verify(validationRepository).save(saved.capture());

        ArgumentCaptor<ActivationCodeIssued> published = ArgumentCaptor.forClass(ActivationCodeIssued.class);
        verify(eventPublisher).publishEvent(published.capture());

        ActivationCodeIssued event = published.getValue();
        assertThat(event.recipientEmail()).isEqualTo("awa@example.sn");
        assertThat(event.recipientLastname()).isEqualTo("Diop");
        // Le code publié doit être exactement celui persisté, sinon l'utilisateur
        // recevrait un code qui n'ouvre aucun compte.
        assertThat(event.code()).isEqualTo(saved.getValue().getCode());
    }

    @Test
    @DisplayName("génère un code à 6 chiffres et une expiration postérieure à la création")
    void registerUserCode_generatesSixDigitCodeAndExpiry() {
        validationService.registerUserCode(user("awa@example.sn", "Diop"));

        ArgumentCaptor<Validation> saved = ArgumentCaptor.forClass(Validation.class);
        verify(validationRepository).save(saved.capture());
        Validation validation = saved.getValue();

        // Le formatage %06d doit conserver les zéros de tête : « 000042 », jamais « 42 ».
        assertThat(validation.getCode()).matches(SIX_DIGITS);
        assertThat(validation.getExpiration()).isAfter(validation.getCreation());
    }

    @Test
    @DisplayName("un code inconnu lève ResourceNotFoundException et ne publie rien")
    void readByCode_unknownCodeThrows() {
        when(validationRepository.findByCode("000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validationService.readByCode("000000"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(eventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }
}
