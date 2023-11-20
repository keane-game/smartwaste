package ucg.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ucg.collecte.master.dto.HistoryDto;
import ucg.collecte.master.model.History;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HistoryMapper {
    HistoryMapper HMP = Mappers.getMapper(HistoryMapper.class);

    HistoryDto modelToDto(History history);

    History dtoToModel (HistoryDto historyDto);

    List<HistoryDto> listModelToDto(List<History> histories);
}
