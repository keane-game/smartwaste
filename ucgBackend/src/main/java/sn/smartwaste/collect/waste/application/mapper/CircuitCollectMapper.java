package sn.smartwaste.collect.waste.application.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.waste.application.dto.CircuitCollect;
import sn.smartwaste.collect.waste.domain.model.CircuitCollectEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface CircuitCollectMapper {

    CircuitCollectMapper CCMP = Mappers.getMapper(CircuitCollectMapper.class);

    CircuitCollect asDto(CircuitCollectEntity circuitCollect);

    CircuitCollectEntity asModel(CircuitCollect circuitCollect);

    List<CircuitCollect> asListDto(List<CircuitCollectEntity> circuitCollects);
}
