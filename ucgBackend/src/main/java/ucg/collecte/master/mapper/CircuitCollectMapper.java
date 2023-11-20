package ucg.collecte.master.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ucg.collecte.master.dto.CircuitCollectDto;
import ucg.collecte.master.model.CircuitCollect;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CircuitCollectMapper {

    CircuitCollectMapper CCMP = Mappers.getMapper(CircuitCollectMapper.class);

    CircuitCollectDto modelToDto(CircuitCollect circuitCollect);

    CircuitCollect dtoToModel (CircuitCollectDto circuitCollectDto);

    List<CircuitCollectDto> listModelToDto(List<CircuitCollect> circuitCollects);
}
