package ucg.collecte.master;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ucg.collecte.master.dto.RegionDto;
import ucg.collecte.master.model.Region;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RegionMapper {

    RegionMapper RMP = Mappers.getMapper(RegionMapper.class);

    RegionDto modelToDto(Region region);

    Region dtoToModel (RegionDto regionDto);

    List<RegionDto> listModelToDto(List<Region> regions);
}
