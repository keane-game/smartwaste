package sn.smartwaste.collect.waste.application.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.waste.application.dto.Alert;
import sn.smartwaste.collect.waste.domain.model.AlertEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface AlertMapper {
    AlertMapper AMP = Mappers.getMapper(AlertMapper.class);

    AlertEntity asModel(Alert alert);

    Alert asDto(AlertEntity alert);

    List<Alert> asListDto(List<AlertEntity> alerts);
}
