package sonaged.collecte.master.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.CircuitCollect;
import sonaged.collecte.master.model.CircuitCollectEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface CircuitCollectMapper {

    CircuitCollectMapper CCMP = Mappers.getMapper(CircuitCollectMapper.class);

    CircuitCollect asDto(CircuitCollectEntity circuitCollect);

    CircuitCollectEntity asModel(CircuitCollect circuitCollect);

    List<CircuitCollect> asListDto(List<CircuitCollectEntity> circuitCollects);
}
