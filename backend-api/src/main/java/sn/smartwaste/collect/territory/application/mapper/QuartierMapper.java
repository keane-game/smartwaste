package sn.smartwaste.collect.territory.application.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.territory.application.dto.Quartier;
import sn.smartwaste.collect.territory.domain.model.QuartierEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface QuartierMapper {
    QuartierMapper QMP = Mappers.getMapper(QuartierMapper.class);

    // P1-2 : le DTO porte `communeId` et non l'entité — mêmes raisons que CommuneMapper.
    @Mapping(source = "commune.communeId", target = "communeId")
    Quartier asDto(QuartierEntity quartier);

    @Mapping(target = "commune", ignore = true)
    QuartierEntity asModel(Quartier quartier);

    List<Quartier> listModelToDto(List<QuartierEntity> quartiers);
}
