package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.Department;

import java.util.List;

public interface DepartmentService {
    Department getOneDepartment(Long departmentId);

    List<Department> getAllDepartment();

    Department createOneDepartment(Department departmentDto);

    Department updateOneDepartment(Long departmentId, Department departmentDto);
    void deleteOneDepartment(Long departmentId);
}
