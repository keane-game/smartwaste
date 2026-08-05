package sn.smartwaste.collect.waste.application.service;

import sn.smartwaste.collect.waste.application.dto.History;

import java.util.List;

public interface HistoryService {
    History readHistory(Long historyId);

    List<History> readAllHistory();

    History createHistory(History history);

    History updateHistory(Long HistoryId, History history);

    void deleteHistory(Long historyId);
}
