package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.mapper.RegionMapper;
import sonaged.collecte.master.repository.DepartmentRepository;
import sonaged.collecte.master.dto.Department;
import sonaged.collecte.master.mapper.DepartmentMapper;
import sonaged.collecte.master.repository.RegionRepository;
import sonaged.collecte.master.service.DepartmentService;

import java.util.List;
import java.util.Objects;

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
        var departmentList = departmentRepository.findAll();
        return DepartmentMapper.DMP.asListDto(departmentList);
    }


    @Override
    public Department createDepartment(Department department) {
        if(department.getRegion ().getId () != null) {
            var region = regionRepository.findById (department.getRegion ( ).getId ( )).orElseThrow (
                    () -> new ResourceNotFoundException ("")
            );
            department.setRegion (RegionMapper.RMP.asDto(region));
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
        departmentRepository.delete(department);
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
