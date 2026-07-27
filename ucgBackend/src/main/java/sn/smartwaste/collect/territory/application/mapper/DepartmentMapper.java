package sn.smartwaste.collect.territory.application.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.territory.application.dto.Department;
import sn.smartwaste.collect.territory.domain.model.DepartmentEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface DepartmentMapper {
    DepartmentMapper DMP = Mappers.getMapper(DepartmentMapper.class);

    Department asDto(DepartmentEntity department);

    DepartmentEntity asModel(Department department);

    List<Department> asListDto(List<DepartmentEntity> departments);
}
