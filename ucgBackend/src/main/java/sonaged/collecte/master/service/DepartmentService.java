package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.Department;

import java.util.List;

public interface DepartmentService {
    Department readDepartment(Long departmentId);

    List<Department> readAllDepartment();

    Department createDepartment(Department departmentDto);

    Department updateDepartment(Long departmentId, Department departmentDto);

    void deleteDepartment(Long departmentId);
}
