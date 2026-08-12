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
import sn.smartwaste.collect.territory.application.dto.Quartier;
import sn.smartwaste.collect.territory.domain.model.CommuneEntity;
import sn.smartwaste.collect.territory.domain.model.QuartierEntity;
import sn.smartwaste.collect.territory.domain.repository.CommuneRepository;
import sn.smartwaste.collect.territory.domain.repository.QuartierRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * P1-2 : {@code createQuartier} déréférençait {@code quartier.getCommune().getCommuneId()} sans
 * garde — une création de quartier sans commune (commune absente du corps de la requête) levait
 * une NullPointerException. Le DTO porte désormais {@code communeId} (UUID) plutôt que l'entité
 * {@code CommuneEntity}, sur le même patron que {@code Commune.departmentId}. Ces tests verrouillent
 * les deux chemins (avec/sans commune) et le cas identifiant inconnu.
 */
@ExtendWith(MockitoExtension.class)
class QuartierServiceImplTest {

    @Mock
    private CommuneRepository communeRepository;
    @Mock
    private QuartierRepository quartierRepository;

    @InjectMocks
    private QuartierServiceImpl quartierService;

    private static Quartier quartier(UUID communeId) {
        var quartier = new Quartier();
        quartier.setName("Diamaguene");
        quartier.setCommuneId(communeId);
        return quartier;
    }

    @Test
    @DisplayName("createQuartier sans communeId ne leve pas de NullPointerException")
    void createQuartier_withoutCommuneId_doesNotThrow() {
        when(quartierRepository.save(any(QuartierEntity.class))).thenAnswer(i -> i.getArgument(0));

        var created = quartierService.createQuartier(quartier(null));

        assertThat(created.getName()).isEqualTo("Diamaguene");
        verify(communeRepository, never()).findById(any());
        ArgumentCaptor<QuartierEntity> saved = ArgumentCaptor.forClass(QuartierEntity.class);
        verify(quartierRepository).save(saved.capture());
        assertThat(saved.getValue().getCommune()).isNull();
    }

    @Test
    @DisplayName("createQuartier avec communeId resout et rattache la commune")
    void createQuartier_withCommuneId_attachesCommune() {
        UUID communeId = UUID.randomUUID();
        var commune = new CommuneEntity();
        commune.setCommuneId(communeId);
        when(communeRepository.findById(communeId)).thenReturn(Optional.of(commune));
        when(quartierRepository.save(any(QuartierEntity.class))).thenAnswer(i -> i.getArgument(0));

        quartierService.createQuartier(quartier(communeId));

        ArgumentCaptor<QuartierEntity> saved = ArgumentCaptor.forClass(QuartierEntity.class);
        verify(quartierRepository).save(saved.capture());
        assertThat(saved.getValue().getCommune()).isSameAs(commune);
    }

    @Test
    @DisplayName("createQuartier avec un communeId inconnu leve ResourceNotFoundException")
    void createQuartier_withUnknownCommuneId_throws() {
        UUID communeId = UUID.randomUUID();
        when(communeRepository.findById(communeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quartierService.createQuartier(quartier(communeId)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(quartierRepository, never()).save(any());
    }
}
