package sn.smartwaste.collect.waste.application.service.impl;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import sn.smartwaste.collect.shared.domain.event.AlertRaisedEvent;
import sn.smartwaste.collect.tenant.application.api.CurrentTenantProvider;
import sn.smartwaste.collect.waste.application.dto.Alert;
import sn.smartwaste.collect.waste.application.service.ImageService;
import sn.smartwaste.collect.waste.domain.model.AlertCode;
import sn.smartwaste.collect.waste.domain.model.AlertEntity;
import sn.smartwaste.collect.waste.domain.model.ImageEntity;
import sn.smartwaste.collect.waste.domain.repository.AlertRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verrouille la <b>charge utile</b> de {@link AlertRaisedEvent}.
 *
 * <p>Pourquoi ce test. La migration ADR-0013 a remplacé le DTO {@code Alert} transporté par
 * l'événement par un enregistrement autonome vivant dans le shared kernel — sans quoi le noyau
 * partagé, dont tout le système dépend, dépendait du contexte « Déchets ». Cette charge utile est
 * sérialisée telle quelle dans le flux SSE (`GET /v1/alerts/stream`), et le frontend lit
 * précisément {@code alertId}, {@code object}, {@code message}, {@code address}, {@code code} et
 * {@code image.url} (`list-alert.component.html`, `header.component.ts`). Renommer ou oublier un de
 * ces champs casserait le temps réel <b>en silence</b> : aucun type ne relie le backend au gabarit
 * Angular. D'où l'assertion, champ par champ, sur le JSON réellement émis.
 */
@ExtendWith(MockitoExtension.class)
class AlertRaisedEventPayloadTest {

    @Mock
    private AlertRepository alertRepository;
    @Mock
    private ImageService imageService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private CurrentTenantProvider currentTenantProvider;

    /** Le service reçoit un vrai ObjectMapper : c'est aussi celui qui sérialisera l'événement. */
    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AlertServiceImpl alertService;

    @BeforeEach
    void stubCurrentOrganization() {
        lenient().when(currentTenantProvider.currentOrganizationId())
                .thenReturn(java.util.Optional.of(uuid(1)));
    }

    /** UUID stable dérivé d'un petit entier : les cas restent lisibles, l'entité est en UUID. */
    private static UUID uuid(long n) {
        return UUID.fromString(String.format("00000000-0000-0000-0000-%012d", n));
    }

    private AlertRaisedEvent captureRaisedEvent() {
        ArgumentCaptor<Object> published = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOf(AlertRaisedEvent.class);
        return (AlertRaisedEvent) published.getValue();
    }

    private static AlertEntity savedAlert() {
        ImageEntity image = new ImageEntity();
        image.setUrl("https://minio.local/alerts/photo.jpg");
        image.setName("photo.jpg");

        AlertEntity entity = new AlertEntity();
        entity.setAlertId(uuid(42));
        entity.setObject("Dépôt sauvage");
        entity.setMessage("Amas de gravats");
        entity.setAddress("Pikine Nord");
        entity.setCode(AlertCode.DANGER);
        entity.setImage(image);
        return entity;
    }

    @Test
    @DisplayName("l'événement publié porte les champs lus par le frontend, image comprise")
    void raisedEventCarriesFieldsReadByFrontend() throws IOException {
        when(alertRepository.save(any(AlertEntity.class))).thenReturn(savedAlert());

        alertService.createAlertFile(new Alert());

        AlertRaisedEvent.RaisedAlert payload = captureRaisedEvent().alert();
        assertThat(payload.alertId()).isEqualTo(uuid(42));
        assertThat(payload.object()).isEqualTo("Dépôt sauvage");
        assertThat(payload.message()).isEqualTo("Amas de gravats");
        assertThat(payload.address()).isEqualTo("Pikine Nord");
        // Chaîne et non énumération : le shared kernel ignore le vocabulaire métier de `waste`.
        assertThat(payload.code()).isEqualTo(AlertCode.DANGER.name());
        assertThat(payload.image()).isNotNull();
        assertThat(payload.image().url()).isEqualTo("https://minio.local/alerts/photo.jpg");
    }

    @Test
    @DisplayName("le JSON émis en SSE conserve les noms de champs attendus côté Angular")
    void serializedPayloadKeepsWireFormat() throws IOException {
        when(alertRepository.save(any(AlertEntity.class))).thenReturn(savedAlert());

        alertService.createAlertFile(new Alert());

        String json = objectMapper.writeValueAsString(captureRaisedEvent());

        // Le composant Angular lit `payload.alert.<champ>` et `payload.alert.image.url`.
        assertThat(json).contains("\"alert\":")
                .contains("\"alertId\":\"" + uuid(42) + "\"")
                .contains("\"object\":\"Dépôt sauvage\"")
                .contains("\"message\":\"Amas de gravats\"")
                .contains("\"address\":\"Pikine Nord\"")
                .contains("\"code\":\"DANGER\"")
                .contains("\"url\":\"https://minio.local/alerts/photo.jpg\"")
                .contains("\"source\":\"MANUAL\"");
        // `file` était un MultipartFile : un flux de requête HTTP n'a rien à faire dans une charge
        // utile sérialisée, et il ne survit pas à la fin de la requête.
        assertThat(json).doesNotContain("\"file\"");
    }

    @Test
    @DisplayName("une alerte sans image ni code produit une charge utile valide, sans NPE")
    void raisedEventToleratesMissingImageAndCode() throws IOException {
        AlertEntity bare = new AlertEntity();
        bare.setAlertId(uuid(7));
        bare.setObject("Sans image");
        when(alertRepository.save(any(AlertEntity.class))).thenReturn(bare);

        alertService.createAlertFile(new Alert());

        AlertRaisedEvent.RaisedAlert payload = captureRaisedEvent().alert();
        assertThat(payload.alertId()).isEqualTo(uuid(7));
        assertThat(payload.code()).isNull();
        assertThat(payload.image()).isNull();
    }
}
