package sonaged.collecte.master.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sonaged.collecte.master.dto.Alert;

import java.util.List;

public interface AlertService {
    Alert readAlert(Long alertId);

    List<Alert> readAllAlert();

    Alert createAlert(Alert alert);

    Alert updateAlert(Long AlertId, Alert alert);

    void deleteAlert(Long alertId);

    Page<Alert> readAllAlert(Pageable pageable);

}
