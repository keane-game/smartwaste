package ucg.collecte.master;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ucg.collecte.master.dto.CommuneDto;
import ucg.collecte.master.model.Commune;

import java.util.List;


@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommuneMapper {
    CommuneMapper COMP = Mappers.getMapper(CommuneMapper.class);

    CommuneDto modelToDto(Commune commune);

    Commune dtoToModel (CommuneDto communeDto);

    List<CommuneDto> listModelToDto(List<Commune> communes);
}
