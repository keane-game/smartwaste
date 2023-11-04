package ucg.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ucg.collecte.master.dto.DepotDto;
import ucg.collecte.master.model.Depot;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepotMapper {

    DepotMapper DETMP = Mappers.getMapper(DepotMapper.class);

    DepotDto modelToDto(Depot depot);

    Depot dtoToModel (DepotDto depotDto);

    List<DepotDto> listModelToDto(List<Depot> depots);
}
