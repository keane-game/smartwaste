package sn.smartwaste.collect.territory.application.service.impl;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.smartwaste.collect.territory.application.api.DepartmentMaps;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.territory.application.mapper.CoordinateMapper;
import sn.smartwaste.collect.territory.application.mapper.RegionMapper;
import sn.smartwaste.collect.territory.domain.repository.DepartmentRepository;
import sn.smartwaste.collect.territory.application.dto.Department;
import sn.smartwaste.collect.territory.application.mapper.DepartmentMapper;
import sn.smartwaste.collect.territory.domain.repository.RegionRepository;
import sn.smartwaste.collect.territory.application.service.DepartmentService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// P1-2 : DepartmentMapper.asDto lit les associations lazy `region`/`communes` — même correctif
// que RegionServiceImpl (dépendait implicitement d'open-in-view).
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {
    private final RegionRepository regionRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional(readOnly = true)
    public Department readDepartment(UUID departmentId) {
        var department  = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department with id [%s] not found ".formatted(departmentId)
                ));
        return DepartmentMapper.DMP.asDto(department);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Department> readAllDepartment() {
        var departmentList = departmentRepository.findByDeletionStatus(sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE);
        return DepartmentMapper.DMP.asListDto(departmentList);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentMaps getFirstDepartment() {
        var department = departmentRepository.findFirstByOrderByNameAsc ();
        if (department == null){
            return null;
        }
        var departmentMaps = new DepartmentMaps (  );
        departmentMaps.setName (department.getName ());
        departmentMaps.setCode (department.getCode ());
        // Géométrie facultative : sans garde, un département sans contour faisait échouer le fond
        // de carte en 500. On rend le département sans tracé plutôt que rien du tout.
        var geometry = department.getGeometry ();
        if (geometry != null) {
            departmentMaps.setTypeGeo (geometry.getType ());
            departmentMaps.setCoordinates (CoordinateMapper.CODMP.asListDto (geometry.getCoordinates () ));
        }

        return departmentMaps;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Department> readAllDepartment(Pageable pageable) {
        return departmentRepository.findByDeletionStatus (sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE, pageable).map (DepartmentMapper.DMP::asDto);
    }

    @Override
    public Department createDepartment(Department department) {
        // P1-2 : le DTO porte désormais `regionId` (le mapper ignore l'association côté
        // entité). Au passage, corrige une NullPointerException systématique quand un
        // département était créé sans région (déréférencement de `getRegion()` sans garde).
        var departmentEntity = DepartmentMapper.DMP.asModel(department);
        if (department.getRegionId() != null) {
            var region = regionRepository.findById(department.getRegionId()).orElseThrow(
                    () -> new ResourceNotFoundException(
                            "Region with id [%s] not found".formatted(department.getRegionId()))
            );
            departmentEntity.setRegion(region);
        }

        var departmentSave = departmentRepository.save(departmentEntity);
        return DepartmentMapper.DMP.asDto(departmentSave);
    }


    @Override
    public Department updateDepartment(UUID departmentId, Department department) {
        var depDB = departmentRepository.findById(departmentId).get();
        if(Objects.nonNull(department.getName()) &&
                !"".equalsIgnoreCase(department.getName())) {
            depDB.setName(department.getName());
        }
        if(Objects.nonNull(department.getCode()) &&
                !"".equalsIgnoreCase(department.getCode())) {
            depDB.setCode(department.getCode());
        }
        return DepartmentMapper.DMP.asDto(departmentRepository.save(depDB));
    }


    @Override
    public void deleteDepartment(UUID departmentId) {
        var department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department with id [%s] not found ".formatted(departmentId)
                ));
        department.markForDeletion(java.time.LocalDateTime.now()); departmentRepository.save(department); // soft-delete (rétention + purge planifiée)
    }


    /**
     public Department updateDepartment(UUID departmentId, Department department) {
     Department depDB = departmentRepository.findById(departmentId).get();

     if(Objects.nonNull(department.getName()) &&
     !"".equalsIgnoreCase(department.getName())) {
     depDB.setName(department.getName());
     }

     if(Objects.nonNull(department.getCode()) &&
     !"".equalsIgnoreCase(department.getCode())) {
     depDB.setCode(department.getCode());
     }

     if(Objects.nonNull(department.getAddress()) &&
     !"".equalsIgnoreCase(department.getAddress())) {
     depDB.setAddress(department.getAddress());
     }

     return departmentRepository.save(depDB);
     }
     ======
     public Department fetchDepartmentById(UUID departmentId) throws DepartmentNotFoundException {
     Optional<Department> department =
     departmentRepository.findById(departmentId);

     if(!department.isPresent()) {
     throw new DepartmentNotFoundException("Department Not Available");
     }

     return  department.get();
     }
     * */


}
