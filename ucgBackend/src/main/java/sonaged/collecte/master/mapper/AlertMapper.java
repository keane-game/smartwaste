package sonaged.collecte.master.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.Alert;
import sonaged.collecte.master.model.AlertEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface AlertMapper {
    AlertMapper AMP = Mappers.getMapper(AlertMapper.class);

    AlertEntity asModel(Alert alert);

    Alert asDto(AlertEntity alert);

    List<Alert> asListDto(List<AlertEntity> alerts);
}
