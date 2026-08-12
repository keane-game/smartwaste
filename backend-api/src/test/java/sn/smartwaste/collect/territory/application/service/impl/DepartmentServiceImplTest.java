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
import sn.smartwaste.collect.territory.application.dto.Department;
import sn.smartwaste.collect.territory.domain.model.DepartmentEntity;
import sn.smartwaste.collect.territory.domain.model.RegionEntity;
import sn.smartwaste.collect.territory.domain.repository.DepartmentRepository;
import sn.smartwaste.collect.territory.domain.repository.RegionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * P1-2 : {@code createDepartment} déréférençait {@code department.getRegion().getRegionId()} sans
 * garde — une création de département sans région (region absente du corps de la requête) levait
 * une NullPointerException. Le DTO porte désormais {@code regionId} (UUID) plutôt que l'entité
 * {@code RegionEntity}, sur le même patron que {@code Commune.departmentId}. Ces tests verrouillent
 * les deux chemins (avec/sans région) et le cas identifiant inconnu.
 */
@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private RegionRepository regionRepository;
    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    @Test
    @DisplayName("createDepartment sans regionId ne leve pas de NullPointerException")
    void createDepartment_withoutRegionId_doesNotThrow() {
        var dto = new Department(null, "Pikine", "PK", null, null, null);
        when(departmentRepository.save(any(DepartmentEntity.class))).thenAnswer(i -> i.getArgument(0));

        var created = departmentService.createDepartment(dto);

        assertThat(created.getName()).isEqualTo("Pikine");
        verify(regionRepository, never()).findById(any());
        ArgumentCaptor<DepartmentEntity> saved = ArgumentCaptor.forClass(DepartmentEntity.class);
        verify(departmentRepository).save(saved.capture());
        assertThat(saved.getValue().getRegion()).isNull();
    }

    @Test
    @DisplayName("createDepartment avec regionId resout et rattache la region")
    void createDepartment_withRegionId_attachesRegion() {
        UUID regionId = UUID.randomUUID();
        var region = new RegionEntity();
        region.setRegionId(regionId);
        var dto = new Department(null, "Pikine", "PK", null, regionId, null);
        when(regionRepository.findById(regionId)).thenReturn(Optional.of(region));
        when(departmentRepository.save(any(DepartmentEntity.class))).thenAnswer(i -> i.getArgument(0));

        departmentService.createDepartment(dto);

        ArgumentCaptor<DepartmentEntity> saved = ArgumentCaptor.forClass(DepartmentEntity.class);
        verify(departmentRepository).save(saved.capture());
        assertThat(saved.getValue().getRegion()).isSameAs(region);
    }

    @Test
    @DisplayName("createDepartment avec un regionId inconnu leve ResourceNotFoundException")
    void createDepartment_withUnknownRegionId_throws() {
        UUID regionId = UUID.randomUUID();
        var dto = new Department(null, "Pikine", "PK", null, regionId, null);
        when(regionRepository.findById(regionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.createDepartment(dto))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(departmentRepository, never()).save(any());
    }
}
