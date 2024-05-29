package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.model.AlertEntity;
import sonaged.collecte.master.repository.AlertRepository;
import sonaged.collecte.master.dto.Alert;
import sonaged.collecte.master.mapper.AlertMapper;
import sonaged.collecte.master.service.AlertService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AlertServiceImpl implements AlertService {


    private final AlertRepository alertRepository;

    @Override
    public Alert getOneAlert(Long alertId) {
        var alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert with id [%s] not found ".formatted(alertId)
                ));
        return AlertMapper.AMP.asDto(alert);
    }


    @Override
    public List<Alert> getAllAlert() {
        List<AlertEntity> alertList = alertRepository.findAll();
        return AlertMapper.AMP.asListDto(alertList);
    }


    @Override
    public Alert createOneAlert(Alert alert) {
        var savedAlert = alertRepository.save(AlertMapper.AMP.asModel(alert));
        return AlertMapper.AMP.asDto (savedAlert);
    }


    @Override
    public Alert updateOneAlert(Long alertId, Alert alert) {
        var existedAlert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert with id [%s] not found ".formatted(alertId)
                ));
        if (alert.getCode() != null){
            existedAlert.setCode(alert.getCode());
        }
        if (alert.getMessage() != null){
            existedAlert.setMessage(alert.getMessage());
        }
        return AlertMapper.AMP.asDto(existedAlert);
    }

    /**
     * @param alertId 
     */
    @Override
    public void deleteOneAlert(Long alertId) {
        var alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert with id [%s] not found ".formatted(alertId)
                ));
        alertRepository.delete(alert);
    }
}
