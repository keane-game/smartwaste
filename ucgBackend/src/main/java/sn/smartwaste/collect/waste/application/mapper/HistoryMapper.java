package sn.smartwaste.collect.waste.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.waste.application.dto.History;
import sn.smartwaste.collect.waste.domain.model.HistoryEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface HistoryMapper {
    HistoryMapper HMP = Mappers.getMapper(HistoryMapper.class);

    History asDto(HistoryEntity history);

    History asModel(History history);

    List<History> asListDto(List<HistoryEntity> histories);
}
