package sonaged.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.HistoryDto;
import sonaged.collecte.master.model.History;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HistoryMapper {
    HistoryMapper HMP = Mappers.getMapper(HistoryMapper.class);

    HistoryDto modelToDto(History history);

    History dtoToModel (HistoryDto historyDto);

    List<HistoryDto> listModelToDto(List<History> histories);
}
