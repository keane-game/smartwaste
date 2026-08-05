package sn.smartwaste.collect.waste.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.waste.application.dto.Circuit;
import sn.smartwaste.collect.waste.domain.model.CircuitEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface CircuitMapper {

    CircuitMapper CIMP = Mappers.getMapper(CircuitMapper.class);

    Circuit asDto(CircuitEntity circuit);

    CircuitEntity asModel(Circuit circuit);

    List<Circuit> asListDto(List<CircuitEntity> circuits);
}
