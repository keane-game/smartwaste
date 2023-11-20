package ucg.collecte.master;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ucg.collecte.master.dto.TypeDepotDto;
import ucg.collecte.master.model.TypeDepot;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TypeDepotMapper {

    TypeDepotMapper TDMP = Mappers.getMapper(TypeDepotMapper.class);

    TypeDepotDto modelToDto(TypeDepot typeDepot);

    TypeDepot dtoToModel (TypeDepotDto typeDepotDto);

    List<TypeDepotDto> listModelToDto(List<TypeDepot> typeDepots);
}
