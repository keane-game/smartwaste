package ucg.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ucg.collecte.master.dto.DepotoirDto;
import ucg.collecte.master.model.Depotoir;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepotoirMapper {

    DepotoirMapper DETMP = Mappers.getMapper(DepotoirMapper.class);

    DepotoirDto modelToDto(Depotoir depot);

    Depotoir dtoToModel (DepotoirDto depotDto);

    List<DepotoirDto> listModelToDto(List<Depotoir> depots);
}
