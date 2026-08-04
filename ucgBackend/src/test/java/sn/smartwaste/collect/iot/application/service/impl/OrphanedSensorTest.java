package sn.smartwaste.collect.iot.application.service.impl;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.iot.domain.model.Sensor;
import sn.smartwaste.collect.iot.domain.repository.SensorRepository;
import sn.smartwaste.collect.iot.domain.repository.VehicleTrackerRepository;
import sn.smartwaste.collect.waste.application.api.WasteReadModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Un capteur rattaché à un point de collecte inexistant (ADR-0012, G7).
 *
 * <p><b>Comment ce défaut est apparu.</b> Le réimport du référentiel supprime puis recrée les
 * points de collecte, qui reçoivent de <b>nouveaux identifiants</b>. Les capteurs, eux, référencent
 * l'ancien — par identifiant et sans clé étrangère, comme l'impose l'ADR-0012 pour une référence
 * cross-contexte. Après un réimport, <b>tous les capteurs pointent vers le vide</b>.
 *
 * <p><b>Et le système ne dit rien.</b> La mesure est authentifiée, acceptée (201) et stockée ;
 * {@code FillLevelProjector} ne trouve pas le point, journalise et s'arrête. Aucun niveau n'est mis
 * à jour, aucun seuil évalué, aucune alerte levée. Pendant ce temps {@code lastSeenAt} est
 * rafraîchi, si bien que la surveillance du silence considère le capteur <b>vivant</b>. Le capteur
 * croit émettre, le point paraît dépourvu de capteur, et personne n'apprend rien : le silence est
 * complet des deux côtés.
 *
 * <p><b>Pourquoi la vérification passe par un port publié.</b> Le contexte {@code iot} ne voit rien
 * de {@code waste} — il ne peut pas savoir si un identifiant de point existe. C'est à {@code waste},
 * qui possède les points, de le dire.
 */
@ExtendWith(MockitoExtension.class)
class OrphanedSensorTest {

    private static final Long POINT = 215L;

    @Mock private SensorRepository sensorRepository;
    @Mock private VehicleTrackerRepository trackerRepository;
    @Mock private WasteReadModel waste;

    private DeviceProvisioningServiceImpl service() {
        return new DeviceProvisioningServiceImpl(sensorRepository, trackerRepository, waste);
    }

    private Sensor sensor(Long depotoirId) {
        var s = new Sensor();
        s.setSensorId(UUID.randomUUID());
        s.setDeviceCode("CAPTEUR-TEST");
        s.setDepotoirId(depotoirId);
        s.setActive(true);
        return s;
    }

    @Test
    @DisplayName("on n'enrole pas un capteur sur un point de collecte inexistant")
    void enrollmentRefusesAnUnknownPoint() {
        // Fermer la porte a l'entree : sans cela, une faute de frappe dans l'identifiant produit un
        // capteur qui emettra dans le vide sans que rien ne proteste.
        when(waste.collectionPointExists(POINT)).thenReturn(false);

        assertThatThrownBy(() -> service().enrollSensor("CAPTEUR-NEUF", POINT))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("215");

        verify(sensorRepository, never()).save(any());
    }

    @Test
    @DisplayName("un point existant laisse l'enrolement se faire")
    void enrollmentAcceptsAKnownPoint() {
        when(waste.collectionPointExists(POINT)).thenReturn(true);
        when(sensorRepository.findByDeviceCode("CAPTEUR-NEUF"))
                .thenReturn(java.util.Optional.empty());
        when(sensorRepository.save(any(Sensor.class)))
                .thenAnswer(i -> i.getArgument(0));

        var provisioned = service().enrollSensor("CAPTEUR-NEUF", POINT);

        assertThat(provisioned.apiKey()).isNotBlank();
        verify(sensorRepository).save(any(Sensor.class));
    }

    @Test
    @DisplayName("le parc signale les capteurs devenus orphelins")
    void listingFlagsOrphans() {
        // Le cas du reimport : le capteur etait valide a l'enrolement et ne l'est plus. Seule la
        // liste du parc peut le rattraper — la porte d'entree ne suffit pas.
        lenient().when(sensorRepository.findAll())
                .thenReturn(List.of(sensor(POINT), sensor(999L)));
        when(waste.collectionPointExists(POINT)).thenReturn(true);
        when(waste.collectionPointExists(999L)).thenReturn(false);

        var parc = service().listSensors();

        assertThat(parc).hasSize(2);
        assertThat(parc.stream().filter(d -> d.orphaned()).count()).isEqualTo(1);
    }
}
