package sonaged.collecte.master.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sonaged.collecte.master.dto.Department;

import java.util.List;

public interface DepartmentService {
    Department readDepartment(Long departmentId);

    List<Department> readAllDepartment();

    Department createDepartment(Department departmentDto);

    Department updateDepartment(Long departmentId, Department departmentDto);

    void deleteDepartment(Long departmentId);

    Page<Department> readAllDepartment(Pageable pageable);
}
