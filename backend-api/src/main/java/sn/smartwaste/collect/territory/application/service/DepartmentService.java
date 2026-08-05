package sn.smartwaste.collect.territory.application.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.smartwaste.collect.territory.application.dto.Department;
import sn.smartwaste.collect.territory.application.api.DepartmentMaps;

import java.util.List;

public interface DepartmentService {
    Department readDepartment(UUID departmentId);

    List<Department> readAllDepartment();

    Department createDepartment(Department departmentDto);

    Department updateDepartment(UUID departmentId, Department departmentDto);

    void deleteDepartment(UUID departmentId);

    Page<Department> readAllDepartment(Pageable pageable);


    DepartmentMaps getFirstDepartment();

}
