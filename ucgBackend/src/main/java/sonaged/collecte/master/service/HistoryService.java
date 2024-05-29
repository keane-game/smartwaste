package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.History;

import java.util.List;

public interface HistoryService {
    History getOneHistory(Long historyId);

    List<History> getAllHistory();

    History createOneHistory(History history);

    History updateOneHistory(Long HistoryId, History history);
    void deleteOneHistory(Long historyId);
}
