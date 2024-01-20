package sonaged.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.RegionDto;
import sonaged.collecte.master.model.Region;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RegionMapper {

    RegionMapper RMP = Mappers.getMapper(RegionMapper.class);

    RegionDto modelToDto(Region region);

    Region dtoToModel (RegionDto regionDto);

    List<RegionDto> listModelToDto(List<Region> regions);
}
