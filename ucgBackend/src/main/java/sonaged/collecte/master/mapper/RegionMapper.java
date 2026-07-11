package sonaged.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.Region;
import sonaged.collecte.master.model.RegionEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface RegionMapper {

    RegionMapper RMP = Mappers.getMapper(RegionMapper.class);

    Region asDto(RegionEntity region);

    RegionEntity asModel(Region region);

    List<Region> asListDto(List<RegionEntity> regions);
}
