package ucg.collecte.master.service;

import ucg.collecte.master.dto.DepartmentDto;

import java.util.List;

public interface DepartmentService {
    DepartmentDto getOneDepartment(Long departmentId);

    List<DepartmentDto> getAllDepartment();

    DepartmentDto createOneDepartment(DepartmentDto departmentDto);

    DepartmentDto updateOneDepartment(Long DepartmentId, DepartmentDto departmentDto);
    void deleteOneDepartment(Long departmentId);
}
