package sonaged.collecte.master.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.Department;
import sonaged.collecte.master.model.DepartmentEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface DepartmentMapper {
    DepartmentMapper DMP = Mappers.getMapper(DepartmentMapper.class);

    Department asDto(DepartmentEntity department);

    DepartmentEntity asModel(Department department);

    List<Department> asListDto(List<DepartmentEntity> departments);
}
