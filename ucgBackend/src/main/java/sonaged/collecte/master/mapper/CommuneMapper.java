package sonaged.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.CommuneDto;
import sonaged.collecte.master.model.Commune;

import java.util.List;


@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommuneMapper {
    CommuneMapper COMP = Mappers.getMapper(CommuneMapper.class);

    CommuneDto modelToDto(Commune commune);

    Commune dtoToModel (CommuneDto communeDto);

    List<CommuneDto> listModelToDto(List<Commune> communes);
}
