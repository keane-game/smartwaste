package sonaged.collecte.master.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.dto.HistoryDto;
import sonaged.collecte.master.service.HistoryService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class HistoryServiceImpl implements HistoryService {

    /**
     * @param historyId 
     * @return
     */
    @Override
    public HistoryDto getOneHistory(Long historyId) {
        return null;
    }

    /**
     * @return 
     */
    @Override
    public List<HistoryDto> getAllHistory() {
        return null;
    }

    /**
     * @param historyDto 
     * @return
     */
    @Override
    public HistoryDto createOneHistory(HistoryDto historyDto) {
        return null;
    }

    /**
     * @param HistoryId 
     * @param historyDto
     * @return
     */
    @Override
    public HistoryDto updateOneHistory(Long HistoryId, HistoryDto historyDto) {
        return null;
    }

    /**
     * @param historyId 
     */
    @Override
    public void deleteOneHistory(Long historyId) {

    }
}
