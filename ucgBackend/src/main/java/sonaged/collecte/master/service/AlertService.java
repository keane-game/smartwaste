package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.Alert;

import java.util.List;

public interface AlertService {
    Alert getOneAlert(Long alertId);

    List<Alert> getAllAlert();

    Alert createOneAlert(Alert alert);

    Alert updateOneAlert(Long AlertId, Alert alert);

    void deleteOneAlert(Long alertId);
}
