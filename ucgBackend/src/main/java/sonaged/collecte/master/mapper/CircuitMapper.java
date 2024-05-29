package sonaged.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.Circuit;
import sonaged.collecte.master.model.CircuitEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface CircuitMapper {

    CircuitMapper CIMP = Mappers.getMapper(CircuitMapper.class);

    Circuit asDto(CircuitEntity circuit);

    CircuitEntity asModel(Circuit circuit);

    List<Circuit> asListDto(List<CircuitEntity> circuits);
}
