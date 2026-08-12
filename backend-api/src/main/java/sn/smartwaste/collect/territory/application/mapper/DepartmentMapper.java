package sn.smartwaste.collect.territory.application.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.territory.application.dto.Department;
import sn.smartwaste.collect.territory.domain.model.CommuneEntity;
import sn.smartwaste.collect.territory.domain.model.DepartmentEntity;

import java.util.List;
import java.util.UUID;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface DepartmentMapper {
    DepartmentMapper DMP = Mappers.getMapper(DepartmentMapper.class);

    // P1-2 : le DTO porte `communeIds`/`regionId` et non les entités — mêmes raisons que
    // CommuneMapper (fuite d'entité JPA + accès lazy non maîtrisé).
    @Mapping(source = "region.regionId", target = "regionId")
    @Mapping(source = "communes", target = "communeIds")
    Department asDto(DepartmentEntity department);

    // Sens inverse : le rattachement (region) relève du service, comme pour Commune/Department.
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "communes", ignore = true)
    DepartmentEntity asModel(Department department);

    List<Department> asListDto(List<DepartmentEntity> departments);

    default UUID map(CommuneEntity commune) {
        return commune == null ? null : commune.getCommuneId();
    }
}
