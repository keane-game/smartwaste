package sn.smartwaste.collect.waste.application.service.impl;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.shared.domain.model.ImportedFeature;
import sn.smartwaste.collect.waste.domain.model.TypeDepotoirEntity;
import sn.smartwaste.collect.waste.domain.repository.CircuitBalayageRepository;
import sn.smartwaste.collect.waste.domain.repository.CircuitCollectRepository;
import sn.smartwaste.collect.waste.domain.repository.DepotoirRepository;
import sn.smartwaste.collect.waste.domain.repository.TypeDepotoirRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Un type de point de collecte inconnu doit être <b>persisté</b> avant d'être référencé.
 *
 * <p><b>Le défaut fermé ici.</b> {@code resolveOrCreateType} construisait un
 * {@link TypeDepotoirEntity} sans jamais l'enregistrer, puis {@code importDepotoir} appelait
 * {@code saveAndFlush} sur un dépôt qui le référence. Comme {@code Depotoir → TypeDepotoir} ne
 * cascade volontairement pas en {@code PERSIST} — c'est un référentiel partagé, et P1-2 a réduit la
 * cascade à {@code REFRESH/MERGE} pour qu'un dépôt ne puisse pas écraser une valeur de référence —
 * Hibernate levait :
 * <pre>TransientObjectException: persistent instance references an unsaved transient instance
 * of TypeDepotoirEntity</pre>
 *
 * <p>Le défaut était en place depuis l'origine et n'avait jamais pu se manifester : la table
 * {@code typedepotoir} est vide et l'import n'avait jamais tourné (bloqué d'abord par la dérive de
 * schéma, puis par l'absence de région). Il est apparu au premier import réel — c'est-à-dire au
 * premier moment où il <i>pouvait</i> apparaître.
 */
@ExtendWith(MockitoExtension.class)
class WasteImportAdapterTypeTest {

    @Mock private CircuitCollectRepository circuitCollectRepository;
    @Mock private CircuitBalayageRepository circuitBalayageRepository;
    @Mock private DepotoirRepository depotoirRepository;
    @Mock private TypeDepotoirRepository typeDepotoirRepository;

    @Test
    @DisplayName("un type absent du référentiel est enregistré, pas seulement instancié")
    void persistsAnUnknownType() {
        when(typeDepotoirRepository.findByNameIgnoreCase("Bac de rue")).thenReturn(null);
        when(typeDepotoirRepository.saveAndFlush(any(TypeDepotoirEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        adapter().importDepotoir(feature("Bac de rue"), UUID.randomUUID());

        // Sans cet enregistrement, le flush du dépôt échoue sur une référence transiente.
        verify(typeDepotoirRepository).saveAndFlush(any(TypeDepotoirEntity.class));
    }

    @Test
    @DisplayName("un type déjà connu n'est pas réenregistré")
    void reusesAKnownType() {
        var existant = new TypeDepotoirEntity();
        existant.setName("PP");
        when(typeDepotoirRepository.findByNameIgnoreCase("PP")).thenReturn(existant);

        adapter().importDepotoir(feature("PP"), UUID.randomUUID());

        verify(typeDepotoirRepository, org.mockito.Mockito.never())
                .saveAndFlush(any(TypeDepotoirEntity.class));
    }

    private WasteImportAdapter adapter() {
        return new WasteImportAdapter(circuitCollectRepository, circuitBalayageRepository,
                depotoirRepository, typeDepotoirRepository);
    }

    private ImportedFeature feature(String type) {
        return new ImportedFeature(
                Map.of("Type_de_Mo", type, "Adresse_de", "quelque part"), null);
    }
}
