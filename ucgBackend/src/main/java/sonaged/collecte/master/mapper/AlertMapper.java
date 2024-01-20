package sonaged.collecte.master.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.AlertDto;
import sonaged.collecte.master.model.Alert;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AlertMapper {
    AlertMapper AMP = Mappers.getMapper(AlertMapper.class);

    AlertDto modelToDto(Alert alert);

    Alert dtoToModel (AlertDto alertDto);

    List<AlertDto> listModelToDto(List<Alert> alerts);
}
