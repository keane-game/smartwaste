package sn.smartwaste.collect.territory.application.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.territory.application.dto.Region;
import sn.smartwaste.collect.territory.domain.model.RegionEntity;
import sn.smartwaste.collect.territory.domain.repository.GeometryRepository;
import sn.smartwaste.collect.territory.domain.repository.RegionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code updateRegion}/{@code deleteRegion} étaient des stubs (`return null` / corps vide) — présents
 * dans l'interface et l'implémentation, jamais exposés par {@code RegionController}, jamais écrits
 * (audit 2026-08-10, `docs/FRONTEND_API_MAPPING.md`). Ces tests verrouillent le comportement réel,
 * sur le même patron que les autres niveaux du référentiel territorial (Commune, Département).
 */
@ExtendWith(MockitoExtension.class)
class RegionServiceImplTest {

    @Mock
    private RegionRepository regionRepository;
    @Mock
    private GeometryRepository geometryRepository;

    @InjectMocks
    private RegionServiceImpl regionService;

    private static RegionEntity region(UUID id, String name, String code) {
        var region = new RegionEntity();
        region.setRegionId(id);
        region.setName(name);
        region.setCode(code);
        return region;
    }

    @Test
    @DisplayName("updateRegion applique les champs fournis et ignore les champs absents")
    void updateRegion_patchesProvidedFieldsOnly() {
        UUID id = UUID.randomUUID();
        var existing = region(id, "Dakar", "DK");
        when(regionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(regionRepository.save(any(RegionEntity.class))).thenAnswer(i -> i.getArgument(0));

        var updated = regionService.updateRegion(id, new Region(null, "Nouveau nom", null, null, null));

        assertThat(updated.getName()).isEqualTo("Nouveau nom");
        // Un champ nul est une absence de modification, pas un effacement.
        assertThat(updated.getCode()).isEqualTo("DK");
    }

    @Test
    @DisplayName("updateRegion sur un identifiant inconnu leve ResourceNotFoundException")
    void updateRegion_unknownIdThrows() {
        UUID id = UUID.randomUUID();
        when(regionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> regionService.updateRegion(id, new Region()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(regionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteRegion marque la region en PENDING_DELETION au lieu de la supprimer")
    void deleteRegion_isSoftDelete() {
        UUID id = UUID.randomUUID();
        var existing = region(id, "Dakar", "DK");
        when(regionRepository.findById(id)).thenReturn(Optional.of(existing));

        regionService.deleteRegion(id);

        ArgumentCaptor<RegionEntity> saved = ArgumentCaptor.forClass(RegionEntity.class);
        verify(regionRepository).save(saved.capture());
        assertThat(saved.getValue().isPendingDeletion()).isTrue();
        verify(regionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteRegion sur un identifiant inconnu leve ResourceNotFoundException")
    void deleteRegion_unknownIdThrows() {
        UUID id = UUID.randomUUID();
        when(regionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> regionService.deleteRegion(id))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(regionRepository, never()).save(any());
    }
}
