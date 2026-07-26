package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.dto.maps.DepartmentMaps;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.mapper.CoordinateMapper;
import sonaged.collecte.master.mapper.RegionMapper;
import sonaged.collecte.master.repository.DepartmentRepository;
import sonaged.collecte.master.dto.Department;
import sonaged.collecte.master.mapper.DepartmentMapper;
import sonaged.collecte.master.repository.RegionRepository;
import sonaged.collecte.master.service.DepartmentService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {
    private final RegionRepository regionRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public Department readDepartment(Long departmentId) {
        var department  = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department with id [%s] not found ".formatted(departmentId)
                ));
        return DepartmentMapper.DMP.asDto(department);
    }


    @Override
    public List<Department> readAllDepartment() {
        var departmentList = departmentRepository.findByDeletionStatus(sonaged.collecte.master.enums.DeletionStatus.ACTIVE);
        return DepartmentMapper.DMP.asListDto(departmentList);
    }

    @Override
    public DepartmentMaps getFirstDepartment() {
        var department = departmentRepository.findFirstByOrderByNameAsc ();
        if (department == null){
            return null;
        }
        var departmentMaps = new DepartmentMaps (  );
        departmentMaps.setName (department.getName ());
        departmentMaps.setCode (department.getCode ());
        departmentMaps.setTypeGeo (department.getGeometry ().getType ());
        departmentMaps.setCoordinates (CoordinateMapper.CODMP.asListDto (department.getGeometry().getCoordinates () ));

        return departmentMaps;
    }

    @Override
    public Page<Department> readAllDepartment(Pageable pageable) {
        return departmentRepository.findByDeletionStatus (sonaged.collecte.master.enums.DeletionStatus.ACTIVE, pageable).map (DepartmentMapper.DMP::asDto);
    }

    @Override
    public Department createDepartment(Department department) {
        if(department.getRegion ().getRegionId () != null) {
            var region = regionRepository.findById (department.getRegion ( ).getRegionId ( )).orElseThrow (
                    () -> new ResourceNotFoundException ("")
            );
            department.setRegion (region);
        }

        var departmentSave = departmentRepository.save(DepartmentMapper.DMP.asModel(department));
        return DepartmentMapper.DMP.asDto(departmentSave);
    }


    @Override
    public Department updateDepartment(Long departmentId, Department department) {
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
    public void deleteDepartment(Long departmentId) {
        var department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department with id [%s] not found ".formatted(departmentId)
                ));
        department.markForDeletion(java.time.LocalDateTime.now()); departmentRepository.save(department); // soft-delete (rétention + purge planifiée)
    }


    /**
     public Department updateDepartment(Long departmentId, Department department) {
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
     public Department fetchDepartmentById(Long departmentId) throws DepartmentNotFoundException {
     Optional<Department> department =
     departmentRepository.findById(departmentId);

     if(!department.isPresent()) {
     throw new DepartmentNotFoundException("Department Not Available");
     }

     return  department.get();
     }
     * */


}
