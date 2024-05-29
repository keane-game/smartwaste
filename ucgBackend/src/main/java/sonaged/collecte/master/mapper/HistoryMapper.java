package sonaged.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.History;
import sonaged.collecte.master.model.HistoryEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface HistoryMapper {
    HistoryMapper HMP = Mappers.getMapper(HistoryMapper.class);

    History asDto(HistoryEntity history);

    History asModel(History history);

    List<History> asListDto(List<HistoryEntity> histories);
}
