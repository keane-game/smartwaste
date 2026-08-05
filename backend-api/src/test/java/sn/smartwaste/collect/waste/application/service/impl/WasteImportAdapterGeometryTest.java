package sn.smartwaste.collect.waste.application.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.shared.domain.model.ImportedFeature;
import sn.smartwaste.collect.territory.application.api.TerritoryImportPort;
import sn.smartwaste.collect.territory.domain.model.CoordinateEntity;
import sn.smartwaste.collect.territory.domain.model.GeometryEntity;
import sn.smartwaste.collect.waste.domain.model.CircuitCollectEntity;
import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;
import sn.smartwaste.collect.waste.domain.model.TypeDepotoirEntity;
import sn.smartwaste.collect.waste.domain.repository.CircuitBalayageRepository;
import sn.smartwaste.collect.waste.domain.repository.CircuitCollectRepository;
import sn.smartwaste.collect.waste.domain.repository.DepotoirRepository;
import sn.smartwaste.collect.waste.domain.repository.TypeDepotoirRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Les entités du contexte « déchets » reçoivent leur géométrie à l'import (ADR-0016).
 *
 * <p><b>Le défaut fermé ici.</b> {@code WasteImportAdapter} ne posait aucune géométrie, au motif
 * — écrit dans sa javadoc — qu'elle « appartient au référentiel territorial ». Le premier import
 * réel a montré ce que cela produit : <b>dépotoirs 0/71, circuits de collecte 0/52, circuits de
 * balayage 0/156</b> sans coordonnées, quand les communes et quartiers sont à 12/12 et 357/357.
 *
 * <p>Conséquences en chaîne : {@code GET /v1/maps/depotoirs} renvoie {@code []}, aucun circuit
 * n'est traçable, et {@code CollectionRouteServiceImpl} ne peut pas ordonner une tournée
 * géographiquement — il trie sur la priorité de remplissage et l'ancienneté de la mesure, faute de
 * position. Un point de collecte sans position n'est pas supervisable : c'est la donnée qui fait
 * de lui un point.
 */
@ExtendWith(MockitoExtension.class)
class WasteImportAdapterGeometryTest {

    @Mock private CircuitCollectRepository circuitCollectRepository;
    @Mock private CircuitBalayageRepository circuitBalayageRepository;
    @Mock private DepotoirRepository depotoirRepository;
    @Mock private TypeDepotoirRepository typeDepotoirRepository;
    @Mock private TerritoryImportPort territory;

    private static final String LAT = "14.7645";
    private static final String LON = "-17.3900";

    private void territoryBuildsGeometry() {
        lenient().when(territory.newGeometry(any(ImportedFeature.class))).thenAnswer(invocation -> {
            ImportedFeature f = invocation.getArgument(0);
            var geometry = new GeometryEntity();
            geometry.setType(f.geometry().type());
            var coordinate = new CoordinateEntity();
            coordinate.setLatitude(f.geometry().points().getFirst().latitude());
            coordinate.setLongitude(f.geometry().points().getFirst().longitude());
            geometry.setCoordinates(List.of(coordinate));
            return geometry;
        });
    }

    @Test
    @DisplayName("un point de collecte importé porte sa position")
    void depotoirGetsItsGeometry() {
        territoryBuildsGeometry();
        var type = new TypeDepotoirEntity();
        when(typeDepotoirRepository.findByNameIgnoreCase("PP")).thenReturn(type);

        adapter().importDepotoir(feature("esriGeometryPoint"), UUID.randomUUID());

        var saved = ArgumentCaptor.forClass(DepotoirEntity.class);
        verify(depotoirRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getGeometry()).isNotNull();
        assertThat(saved.getValue().getGeometry().getCoordinates().getFirst().getLatitude())
                .isEqualTo(LAT);
    }

    @Test
    @DisplayName("un circuit de collecte importé porte son tracé")
    void circuitCollectGetsItsGeometry() {
        // Sans tracé, un circuit n'est qu'un nom : la carte ne peut rien en dessiner.
        territoryBuildsGeometry();

        adapter().importCircuitCollect(feature("esriGeometryPolyline"), UUID.randomUUID());

        var saved = ArgumentCaptor.forClass(CircuitCollectEntity.class);
        verify(circuitCollectRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getGeometry()).isNotNull();
    }

    @Test
    @DisplayName("une entité sans contour reste importable")
    void toleratesAFeatureWithoutShape() {
        // Le référentiel source n'est pas parfait ; refuser l'entité entière parce que son contour
        // manque perdrait son adresse, son type et sa commune.
        when(territory.newGeometry(any(ImportedFeature.class))).thenReturn(null);
        when(typeDepotoirRepository.findByNameIgnoreCase("PP")).thenReturn(new TypeDepotoirEntity());

        adapter().importDepotoir(new ImportedFeature(Map.of("Type_de_Mo", "PP"), null),
                UUID.randomUUID());

        verify(depotoirRepository).saveAndFlush(any(DepotoirEntity.class));
    }

    private WasteImportAdapter adapter() {
        return new WasteImportAdapter(circuitCollectRepository, circuitBalayageRepository,
                depotoirRepository, typeDepotoirRepository, territory);
    }

    private ImportedFeature feature(String geometryType) {
        return new ImportedFeature(
                Map.of("Type_de_Mo", "PP", "Adresse_de", "quelque part", "Nom", "circuit 1"),
                new ImportedFeature.GeoShape(geometryType, "{\"wkid\":4326}",
                        List.of(new ImportedFeature.GeoPoint(LAT, LON))));
    }
}
