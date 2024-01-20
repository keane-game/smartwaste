package sonaged.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.CircuitBalayageDto;
import sonaged.collecte.master.model.CircuitBalayage;

import java.util.List;


@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CircuitBalayageMapper {
    CircuitBalayageMapper CBMP = Mappers.getMapper(CircuitBalayageMapper.class);

    CircuitBalayageDto modelToDto(CircuitBalayage circuitBalayage);

    CircuitBalayage dtoToModel (CircuitBalayageDto circuitBalayageDto);

    List<CircuitBalayageDto> listModelToDto(List<CircuitBalayage> circuitBalayages);
}
