package sonaged.collecte.master.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.QuartierDto;
import sonaged.collecte.master.model.Quartier;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuartierMapper {
    QuartierMapper QMP = Mappers.getMapper(QuartierMapper.class);

    QuartierDto modelToDto(Quartier quartier);

    Quartier dtoToModel (QuartierDto quartierDto);

    List<QuartierDto> listModelToDto(List<Quartier> quartiers);
}
