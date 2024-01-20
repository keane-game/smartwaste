package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.DepartmentDto;

import java.util.List;

public interface DepartmentService {
    DepartmentDto getOneDepartment(Long departmentId);

    List<DepartmentDto> getAllDepartment();

    DepartmentDto createOneDepartment(DepartmentDto departmentDto);

    DepartmentDto updateOneDepartment(Long DepartmentId, DepartmentDto departmentDto);
    void deleteOneDepartment(Long departmentId);
}
