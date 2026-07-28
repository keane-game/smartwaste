package sn.smartwaste.collect.waste.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.waste.application.dto.CircuitBalayage;
import sn.smartwaste.collect.waste.domain.model.CircuitBalayageEntity;

import java.util.List;


@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface CircuitBalayageMapper {
    CircuitBalayageMapper CBMP = Mappers.getMapper(CircuitBalayageMapper.class);

    CircuitBalayageEntity asModel(CircuitBalayage circuitBalayage);

    CircuitBalayage asDto(CircuitBalayageEntity circuitBalayage);

    List<CircuitBalayage> asListDto(List<CircuitBalayageEntity> circuitBalayages);
}
