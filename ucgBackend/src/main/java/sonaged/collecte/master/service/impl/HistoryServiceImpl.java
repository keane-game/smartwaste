package sonaged.collecte.master.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.dto.History;
import sonaged.collecte.master.service.HistoryService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class HistoryServiceImpl implements HistoryService {


    @Override
    public History readHistory(Long historyId) {
        return null;
    }


    @Override
    public List<History> readAllHistory() {
        return null;
    }


    @Override
    public History createHistory(History historyDto) {
        return null;
    }


    @Override
    public History updateHistory(Long HistoryId, History historyDto) {
        return null;
    }


    @Override
    public void deleteHistory(Long historyId) {

    }
}
