package ucg.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ucg.collecte.master.dto.DepartmentDto;
import ucg.collecte.master.exception.ResourceNotFoundException;
import ucg.collecte.master.mapper.DepartmentMapper;
import ucg.collecte.master.mapper.UserMapper;
import ucg.collecte.master.model.Department;
import ucg.collecte.master.model.User;
import ucg.collecte.master.repository.DepartmentRepository;
import ucg.collecte.master.service.DepartmentService;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    /**
     * @param departmentId 
     * @return
     */
    @Override
    public DepartmentDto getOneDepartment(Long departmentId) {
        Department department  = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found ".formatted(departmentId)
                ));
        return DepartmentMapper.DMP.modelToDto(department);
    }

    /**
     * @return 
     */
    @Override
    public List<DepartmentDto> getAllDepartment() {
        List<Department> departmentList = departmentRepository.findAll();
        return DepartmentMapper.DMP.listModelToDto(departmentList);
    }

    /**
     * @param departmentDto 
     * @return
     */
    @Override
    public DepartmentDto createOneDepartment(DepartmentDto departmentDto) {
        Department department = Department.builder()
                .departmentName(departmentDto.getDepartmentName())
                .departmentCode(departmentDto.getDepartmentCode())
                .build();
        Department departmentSave = departmentRepository.save(department);
        return DepartmentMapper.DMP.modelToDto(departmentSave);
    }

    /**
     * @param DepartmentId 
     * @param departmentDto
     * @return
     */
    @Override
    public DepartmentDto updateOneDepartment(Long DepartmentId, DepartmentDto departmentDto) {
        return null;
    }

    /**
     * @param departmentId 
     */
    @Override
    public void deleteOneDepartment(Long departmentId) {

    }
}
