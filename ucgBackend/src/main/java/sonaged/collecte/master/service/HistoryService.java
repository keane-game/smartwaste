package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.HistoryDto;

import java.util.List;

public interface HistoryService {
    HistoryDto getOneHistory(Long historyId);

    List<HistoryDto> getAllHistory();

    HistoryDto createOneHistory(HistoryDto historyDto);

    HistoryDto updateOneHistory(Long HistoryId, HistoryDto historyDto);
    void deleteOneHistory(Long historyId);
}
