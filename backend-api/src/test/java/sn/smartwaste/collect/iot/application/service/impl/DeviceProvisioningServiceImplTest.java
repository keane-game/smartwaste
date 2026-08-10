package sn.smartwaste.collect.iot.application.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.iot.domain.model.Sensor;
import sn.smartwaste.collect.iot.domain.model.VehicleTracker;
import sn.smartwaste.collect.iot.domain.repository.SensorRepository;
import sn.smartwaste.collect.iot.domain.repository.VehicleTrackerRepository;
import sn.smartwaste.collect.iot.infrastructure.security.DeviceApiKeys;
import sn.smartwaste.collect.tenant.application.api.CurrentTenantProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Enrôlement des équipements de terrain.
 *
 * <p>Un équipement enrôlé est une identité capable d'écrire en base <b>sans compte utilisateur</b>,
 * posée physiquement dans la rue et souvent pour des années. Les garanties testées ici sont celles
 * qui décident si cette identité est solide :
 *
 * <ol>
 *   <li>la clé est <b>générée par le serveur</b>, jamais choisie par l'appelant ;</li>
 *   <li>seule son <b>empreinte</b> est persistée — la clé en clair ne réapparaît nulle part ;</li>
 *   <li>la rotation <b>invalide</b> réellement l'ancienne clé ;</li>
 *   <li>les listes d'administration ne laissent fuiter ni clé ni empreinte.</li>
 * </ol>
 */
@ExtendWith(MockitoExtension.class)
class DeviceProvisioningServiceImplTest {

    private static final UUID SENSOR_ID = UUID.randomUUID();

    /** UUID stable dérivé d'un petit entier : les cas restent lisibles, les entités sont en UUID. */
    private static UUID uuid(long n) {
        return UUID.fromString(String.format("00000000-0000-0000-0000-%012d", n));
    }

    @Mock
    private SensorRepository sensorRepository;
    @Mock
    private VehicleTrackerRepository trackerRepository;
    /** Ces cas portent sur les cles et l'unicite ; l'existence du point est verifiee ailleurs
     *  (OrphanedSensorTest). On se place donc dans le cas nominal : le point existe. */
    @Mock
    private sn.smartwaste.collect.waste.application.api.WasteReadModel waste;
    @Mock
    private CurrentTenantProvider currentTenantProvider;

    @InjectMocks
    private DeviceProvisioningServiceImpl service;

    private void sensorSavesEcho() {
        lenient().when(waste.collectionPointExists(any())).thenReturn(true);
        lenient().when(currentTenantProvider.currentOrganizationId()).thenReturn(Optional.of(UUID.randomUUID()));
        lenient().when(sensorRepository.findByDeviceCode(any())).thenReturn(Optional.empty());
        lenient().when(sensorRepository.save(any(Sensor.class))).thenAnswer(i -> {
            Sensor s = i.getArgument(0);
            s.setSensorId(SENSOR_ID);
            return s;
        });
    }

    private Sensor captureSavedSensor() {
        ArgumentCaptor<Sensor> saved = ArgumentCaptor.forClass(Sensor.class);
        verify(sensorRepository).save(saved.capture());
        return saved.getValue();
    }

    @Test
    @DisplayName("l'enrôlement rend une clé forte que le serveur a générée, et n'en persiste que l'empreinte")
    void enrollmentGeneratesKeyAndStoresOnlyItsHash() {
        sensorSavesEcho();

        var provisioned = service.enrollSensor("ESP-001", uuid(42));

        assertThat(provisioned.apiKey()).isNotBlank();
        // 256 bits en base64url sans padding : 43 caracteres. Une cle courte serait devinable
        // sur un objet accessible physiquement pendant des annees.
        assertThat(provisioned.apiKey()).hasSize(43);

        Sensor saved = captureSavedSensor();
        assertThat(saved.getApiKeyHash()).isEqualTo(DeviceApiKeys.hash(provisioned.apiKey()));
        // La cle en clair ne doit exister QUE dans la reponse.
        assertThat(saved.getApiKeyHash()).isNotEqualTo(provisioned.apiKey());
        assertThat(saved.getDepotoirId()).isEqualTo(uuid(42));
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    @DisplayName("deux enrôlements produisent deux clés différentes")
    void keysAreNotPredictable() {
        sensorSavesEcho();

        assertThat(service.enrollSensor("ESP-001", uuid(1)).apiKey())
                .isNotEqualTo(service.enrollSensor("ESP-002", uuid(2)).apiKey());
    }

    @Test
    @DisplayName("la rotation invalide l'ancienne clé")
    void rotationInvalidatesThePreviousKey() {
        var sensor = new Sensor();
        sensor.setSensorId(SENSOR_ID);
        sensor.setDeviceCode("ESP-001");
        String oldKey = "ancienne-cle";
        sensor.setApiKeyHash(DeviceApiKeys.hash(oldKey));
        when(sensorRepository.findById(SENSOR_ID)).thenReturn(Optional.of(sensor));
        when(sensorRepository.save(any(Sensor.class))).thenAnswer(i -> i.getArgument(0));

        var rotated = service.rotateSensorKey(SENSOR_ID);

        assertThat(rotated.apiKey()).isNotEqualTo(oldKey);
        // C'est tout l'objet d'une rotation apres suspicion de compromission : l'ancienne cle ne
        // doit plus ouvrir quoi que ce soit.
        assertThat(sensor.getApiKeyHash())
                .isEqualTo(DeviceApiKeys.hash(rotated.apiKey()))
                .isNotEqualTo(DeviceApiKeys.hash(oldKey));
    }

    @Test
    @DisplayName("un code d'équipement déjà pris est refusé")
    void duplicateDeviceCodeIsRejected() {
        when(waste.collectionPointExists(uuid(1))).thenReturn(true);
        when(sensorRepository.findByDeviceCode("ESP-001")).thenReturn(Optional.of(new Sensor()));

        // Le motif est verifie, pas seulement le type : sans cela le cas passait au vert alors que
        // le controle d'unicite n'etait meme plus atteint — la verification d'existence du point,
        // ajoutee en amont, echouait la premiere sur un mock non stube.
        assertThatThrownBy(() -> service.enrollSensor("ESP-001", uuid(1)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("porte deja le code");
        verify(sensorRepository, never()).save(any());
    }

    @Test
    @DisplayName("code ou cible manquants : refus, aucun équipement fantôme")
    void missingFieldsAreRejected() {
        assertThatThrownBy(() -> service.enrollSensor("  ", uuid(1))).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service.enrollSensor("ESP-001", null)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service.enrollVehicleTracker("GPS-1", null))
                .isInstanceOf(ResponseStatusException.class);
        verify(sensorRepository, never()).save(any());
        verify(trackerRepository, never()).save(any());
    }

    @Test
    @DisplayName("la liste d'administration ne laisse fuiter ni clé ni empreinte")
    void listingNeverExposesKeys() {
        var sensor = new Sensor();
        sensor.setSensorId(SENSOR_ID);
        sensor.setDeviceCode("ESP-001");
        sensor.setDepotoirId(uuid(42));
        sensor.setApiKeyHash(DeviceApiKeys.hash("secret"));
        when(sensorRepository.findAll()).thenReturn(java.util.List.of(sensor));

        var summaries = service.listSensors();

        assertThat(summaries).hasSize(1);
        // L'empreinte suffirait a verifier une cle devinee hors ligne : elle n'a rien a faire
        // dans une reponse d'administration.
        assertThat(summaries.get(0).toString())
                .doesNotContain(DeviceApiKeys.hash("secret"))
                .contains("ESP-001");
    }

    @Test
    @DisplayName("désactiver un équipement le laisse en base : l'historique reste rattaché")
    void deactivationKeepsTheDevice() {
        var tracker = new VehicleTracker();
        tracker.setActive(true);
        UUID trackerId = UUID.randomUUID();
        when(trackerRepository.findById(trackerId)).thenReturn(Optional.of(tracker));

        service.deactivateVehicleTracker(trackerId);

        assertThat(tracker.isActive()).isFalse();
        verify(trackerRepository).save(tracker);
        verify(trackerRepository, never()).delete(any());
    }
}
