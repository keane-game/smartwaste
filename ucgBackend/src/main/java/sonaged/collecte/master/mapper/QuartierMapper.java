package sonaged.collecte.master.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.Quartier;
import sonaged.collecte.master.model.QuartierEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface QuartierMapper {
    QuartierMapper QMP = Mappers.getMapper(QuartierMapper.class);

    Quartier asDto(QuartierEntity quartier);

    QuartierEntity asModel(Quartier quartier);

    List<Quartier> listModelToDto(List<QuartierEntity> quartiers);
}
