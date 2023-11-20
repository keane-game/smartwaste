package ucg.collecte.master.service;

import ucg.collecte.master.dto.AlertDto;

import java.util.List;

public interface AlertService {
    AlertDto getOneAlert(Long alertId);

    List<AlertDto> getAllAlert();

    AlertDto createOneAlert(AlertDto alertDto);

    AlertDto updateOneAlert(Long AlertId, AlertDto alertDto);

    void deleteOneAlert(Long alertId);
}
