package sn.smartwaste.collect.waste.application.service.impl;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.shared.domain.model.DeletionStatus;
import sn.smartwaste.collect.territory.domain.model.CoordinateEntity;
import sn.smartwaste.collect.territory.domain.model.GeometryEntity;
import sn.smartwaste.collect.territory.domain.repository.GeometryRepository;
import sn.smartwaste.collect.territory.domain.repository.QuartierRepository;
import sn.smartwaste.collect.shared.domain.service.SoftDeleteService;
import sn.smartwaste.collect.waste.application.api.DepotoirMaps;
import sn.smartwaste.collect.waste.application.service.CrossContextReferenceValidator;
import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;
import sn.smartwaste.collect.waste.domain.model.TypeDepotoirEntity;
import sn.smartwaste.collect.waste.domain.repository.DepotoirRepository;
import sn.smartwaste.collect.waste.domain.repository.TypeDepotoirRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

/**
 * Read-model de carte des points de collecte (`GET /v1/maps/depotoirs`).
 *
 * <p>Pourquoi ce test. L'ancien code faisait {@code d.getGeometry().getType()} et
 * {@code d.getTypeDepotoir().getName()} sans aucune garde, alors que les deux associations sont
 * facultatives : un dépotoir créé depuis les écrans CRUD n'a ni contour ni type. Un seul
 * enregistrement de ce genre faisait répondre <b>500 à tout l'endpoint</b> — donc carte vide, sans
 * message d'erreur exploitable. Le test fixe le comportement attendu : on omet ce qu'on ne peut pas
 * dessiner, et on rend le reste.
 */
@ExtendWith(MockitoExtension.class)
class DepotoirMapReadModelTest {

    @Mock
    private GeometryRepository geometryRepository;
    @Mock
    private QuartierRepository quartierRepository;
    @Mock
    private TypeDepotoirRepository typeDepotoirRepository;
    @Mock
    private DepotoirRepository depotoirRepository;
    @Mock
    private SoftDeleteService softDeleteService;
    @Mock
    private CrossContextReferenceValidator crossContextReferenceValidator;

    @InjectMocks
    private DepotoirServiceImpl depotoirService;

    private static GeometryEntity geometry(String type) {
        CoordinateEntity point = new CoordinateEntity();
        GeometryEntity geometry = new GeometryEntity();
        geometry.setType(type);
        geometry.setCoordinates(List.of(point));
        return geometry;
    }

    private static DepotoirEntity depotoir(String address, GeometryEntity geometry, String typeName) {
        DepotoirEntity depotoir = new DepotoirEntity();
        depotoir.setAddress(address);
        depotoir.setGeometry(geometry);
        if (typeName != null) {
            TypeDepotoirEntity type = new TypeDepotoirEntity();
            type.setName(typeName);
            depotoir.setTypeDepotoir(type);
        }
        return depotoir;
    }

    private void given(DepotoirEntity... depotoirs) {
        when(depotoirRepository.findByDeletionStatus(DeletionStatus.ACTIVE)).thenReturn(List.of(depotoirs));
    }

    @Test
    @DisplayName("un point de collecte complet est rendu avec son adresse, son type et son contour")
    void completeCollectionPointIsMapped() {
        given(depotoir("Pikine Nord", geometry("Polygon"), "Bac"));

        List<DepotoirMaps> map = depotoirService.getDepotoirMap();

        assertThat(map).hasSize(1);
        assertThat(map.get(0).getAddress()).isEqualTo("Pikine Nord");
        assertThat(map.get(0).getTypeGeo()).isEqualTo("Polygon");
        assertThat(map.get(0).getTypeDepot()).isEqualTo("Bac");
        assertThat(map.get(0).getCoordinates()).hasSize(1);
    }

    @Test
    @DisplayName("un point sans géométrie est omis, et n'empêche pas les autres d'être rendus")
    void collectionPointWithoutGeometryIsSkippedWithoutFailing() {
        given(depotoir("Sans contour", null, "Bac"),
              depotoir("Pikine Nord", geometry("Polygon"), "Bac"));

        List<DepotoirMaps> map = depotoirService.getDepotoirMap();

        // Le point sans contour disparaît de la carte — mais il ne l'emporte pas avec lui.
        assertThat(map).hasSize(1);
        assertThat(map.get(0).getAddress()).isEqualTo("Pikine Nord");
    }

    @Test
    @DisplayName("un point sans type est rendu, avec un type nul plutôt qu'une erreur")
    void collectionPointWithoutTypeIsStillMapped() {
        given(depotoir("Pikine Nord", geometry("Point"), null));

        List<DepotoirMaps> map = depotoirService.getDepotoirMap();

        assertThat(map).hasSize(1);
        assertThat(map.get(0).getTypeDepot()).isNull();
        assertThat(map.get(0).getTypeGeo()).isEqualTo("Point");
    }

    @Test
    @DisplayName("aucun point exploitable : liste vide, jamais d'exception")
    void noUsableCollectionPointYieldsEmptyList() {
        given(depotoir("A", null, null), depotoir("B", null, "Bac"));

        assertThatCode(() -> assertThat(depotoirService.getDepotoirMap()).isEmpty())
                .doesNotThrowAnyException();
    }
}
