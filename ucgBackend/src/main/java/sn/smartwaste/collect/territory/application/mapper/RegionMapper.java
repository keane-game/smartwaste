package sn.smartwaste.collect.territory.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.territory.application.dto.Region;
import sn.smartwaste.collect.territory.domain.model.RegionEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface RegionMapper {

    RegionMapper RMP = Mappers.getMapper(RegionMapper.class);

    Region asDto(RegionEntity region);

    RegionEntity asModel(Region region);

    List<Region> asListDto(List<RegionEntity> regions);
}
