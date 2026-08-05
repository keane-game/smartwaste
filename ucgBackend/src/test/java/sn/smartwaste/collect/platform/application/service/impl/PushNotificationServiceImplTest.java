package sn.smartwaste.collect.platform.application.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.platform.domain.model.DeviceToken;
import sn.smartwaste.collect.platform.domain.repository.DeviceTokenRepository;
import sn.smartwaste.collect.platform.infrastructure.notification.PushTransport;
import sn.smartwaste.collect.shared.domain.model.DeletionStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Envoi d'une notification aux appareils d'un citoyen (G2 du backlog).
 *
 * <p><b>Ce que cela ferme.</b> La seule diffusion était SSE — une connexion HTTP maintenue, avec
 * jeton : bon pour un poste de supervision, inutile pour un téléphone. Le rappel « sortez vos
 * ordures » et les changements d'état d'un signalement existaient côté serveur et n'atteignaient
 * personne dont l'application était fermée.
 *
 * <p><b>Les deux règles qui décident si la fonction est utilisable :</b>
 * <ul>
 *   <li>un envoi qui échoue ne doit <b>rien</b> interrompre — un jeton périmé ne peut pas faire
 *       rater le rappel de tout un quartier ;</li>
 *   <li>un jeton que le fournisseur déclare invalide est <b>révoqué</b>, sinon la file d'envoi se
 *       remplit d'appareils disparus et le taux d'échec devient illisible.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class PushNotificationServiceImplTest {

    private static final UUID CITOYEN = UUID.randomUUID();

    @Mock private DeviceTokenRepository tokenRepository;
    @Mock private PushTransport transport;

    private PushNotificationServiceImpl service() {
        return new PushNotificationServiceImpl(tokenRepository, transport);
    }

    private DeviceToken token(String value) {
        var t = new DeviceToken();
        t.setTokenId(UUID.randomUUID());
        t.setUserId(CITOYEN);
        t.setToken(value);
        return t;
    }

    private void devicesOf(UUID user, DeviceToken... tokens) {
        lenient().when(tokenRepository.findByUserIdInAndDeletionStatus(
                List.of(user), DeletionStatus.ACTIVE)).thenReturn(List.of(tokens));
    }

    @Test
    @DisplayName("chaque appareil du destinataire recoit la notification")
    void everyDeviceIsNotified() {
        // Une personne, deux appareils : le telephone et la tablette doivent sonner tous les deux.
        devicesOf(CITOYEN, token("jeton-telephone"), token("jeton-tablette"));
        when(transport.send(anyString(), any(), any())).thenReturn(PushTransport.Result.DELIVERED);

        var envoyees = service().notify(List.of(CITOYEN), "Sortez vos ordures", "Passage a 08:00");

        assertThat(envoyees).isEqualTo(2);
        verify(transport).send("jeton-telephone", "Sortez vos ordures", "Passage a 08:00");
        verify(transport).send("jeton-tablette", "Sortez vos ordures", "Passage a 08:00");
    }

    @Test
    @DisplayName("un envoi qui echoue n'interrompt pas les suivants")
    void oneFailureDoesNotStopTheRest() {
        // Le cas qui decide de l'utilisabilite : un jeton perime ne peut pas faire rater le rappel
        // de tout un quartier.
        devicesOf(CITOYEN, token("jeton-casse"), token("jeton-valide"));
        when(transport.send("jeton-casse", "titre", "corps"))
                .thenThrow(new RuntimeException("fournisseur injoignable"));
        when(transport.send("jeton-valide", "titre", "corps"))
                .thenReturn(PushTransport.Result.DELIVERED);

        var envoyees = service().notify(List.of(CITOYEN), "titre", "corps");

        assertThat(envoyees).isEqualTo(1);
        verify(transport).send("jeton-valide", "titre", "corps");
    }

    @Test
    @DisplayName("un jeton declare invalide est revoque")
    void invalidTokenIsRevoked() {
        // Sans cela la file d'envoi se remplit d'appareils desinstalles, et le taux d'echec cesse
        // de vouloir dire quelque chose.
        var perime = token("jeton-perime");
        devicesOf(CITOYEN, perime);
        when(transport.send(anyString(), any(), any())).thenReturn(PushTransport.Result.INVALID_TOKEN);

        service().notify(List.of(CITOYEN), "titre", "corps");

        assertThat(perime.getDeletionStatus()).isEqualTo(DeletionStatus.PENDING_DELETION);
        verify(tokenRepository).save(perime);
    }

    @Test
    @DisplayName("un echec temporaire ne revoque rien")
    void transientFailureKeepsTheToken() {
        // Distinguer « ce jeton n'existe plus » de « le reseau est tombe » : confondre les deux
        // desabonnerait des citoyens a la premiere panne du fournisseur.
        var valide = token("jeton-valide");
        devicesOf(CITOYEN, valide);
        when(transport.send(anyString(), any(), any())).thenReturn(PushTransport.Result.FAILED);

        service().notify(List.of(CITOYEN), "titre", "corps");

        assertThat(valide.getDeletionStatus()).isNotEqualTo(DeletionStatus.PENDING_DELETION);
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("un destinataire sans appareil enregistre n'est pas une erreur")
    void recipientWithoutDeviceIsFine() {
        // La majorite des comptes aujourd'hui : personne n'a encore enregistre d'appareil.
        devicesOf(CITOYEN);

        assertThat(service().notify(List.of(CITOYEN), "titre", "corps")).isZero();
        verify(transport, never()).send(anyString(), any(), any());
    }

    @Test
    @DisplayName("l'enregistrement d'un jeton deja connu ne le duplique pas")
    void registeringTwiceKeepsOneDevice() {
        // Une reinstallation redonne le meme jeton : le dupliquer ferait sonner l'appareil deux fois.
        var existant = token("jeton-telephone");
        when(tokenRepository.findByToken("jeton-telephone")).thenReturn(Optional.of(existant));

        service().register(CITOYEN, "jeton-telephone", "ANDROID");

        verify(tokenRepository).save(existant);
        assertThat(existant.getUserId()).isEqualTo(CITOYEN);
    }
}
