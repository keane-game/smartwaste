package sn.smartwaste.collect.waste.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import sn.smartwaste.collect.waste.application.dto.Alert;

import java.io.IOException;
import java.util.List;

public interface AlertService {
    Alert readAlert(Long alertId);

    List<Alert> readAllAlert();

    Alert createAlert(String alert, MultipartFile file) throws IOException;

    Alert createAlert(Alert alert, MultipartFile file) throws IOException;

    Alert createAlertFile(Alert alert) throws IOException;

    Alert updateAlert(Long AlertId, String alert, MultipartFile file) throws IOException;

    void deleteAlert(Long alertId);

    Page<Alert> readAllAlert(Pageable pageable);

}
