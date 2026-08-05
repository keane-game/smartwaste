package sn.smartwaste.collect.territory.application.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.territory.application.dto.Quartier;
import sn.smartwaste.collect.territory.domain.model.QuartierEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface QuartierMapper {
    QuartierMapper QMP = Mappers.getMapper(QuartierMapper.class);

    Quartier asDto(QuartierEntity quartier);

    QuartierEntity asModel(Quartier quartier);

    List<Quartier> listModelToDto(List<QuartierEntity> quartiers);
}
