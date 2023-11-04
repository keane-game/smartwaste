package ucg.collecte.master.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ucg.collecte.master.dto.DepartmentDto;
import ucg.collecte.master.model.Department;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepartmentMapper {
    DepartmentMapper DMP = Mappers.getMapper(DepartmentMapper.class);

    DepartmentDto modelToDto(Department department);

    Department dtoToModel (DepartmentDto departmentDto);

    List<DepartmentDto> listModelToDto(List<Department> departments);
}
