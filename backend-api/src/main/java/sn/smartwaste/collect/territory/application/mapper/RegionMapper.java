package sn.smartwaste.collect.territory.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.territory.application.dto.Region;
import sn.smartwaste.collect.territory.domain.model.DepartmentEntity;
import sn.smartwaste.collect.territory.domain.model.RegionEntity;

import java.util.List;
import java.util.UUID;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface RegionMapper {

    RegionMapper RMP = Mappers.getMapper(RegionMapper.class);

    // P1-2 : le DTO porte `departmentIds` et non les entités — mêmes raisons que CommuneMapper.
    @Mapping(source = "departments", target = "departmentIds")
    Region asDto(RegionEntity region);

    @Mapping(target = "departments", ignore = true)
    RegionEntity asModel(Region region);

    List<Region> asListDto(List<RegionEntity> regions);

    default UUID map(DepartmentEntity department) {
        return department == null ? null : department.getDepartmentId();
    }
}
