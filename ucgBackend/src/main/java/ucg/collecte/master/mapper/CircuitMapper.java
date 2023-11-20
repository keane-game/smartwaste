package ucg.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ucg.collecte.master.dto.CircuitDto;
import ucg.collecte.master.model.Circuit;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CircuitMapper {

    CircuitMapper CIMP = Mappers.getMapper(CircuitMapper.class);

    CircuitDto modelToDto(Circuit circuit);

    Circuit dtoToModel (CircuitDto circuitDto);

    List<CircuitDto> listModelToDto(List<Circuit> circuits);
}
