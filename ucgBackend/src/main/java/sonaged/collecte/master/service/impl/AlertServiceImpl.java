package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.repository.AlertRepository;
import sonaged.collecte.master.dto.AlertDto;
import sonaged.collecte.master.mapper.AlertMapper;
import sonaged.collecte.master.model.Alert;
import sonaged.collecte.master.service.AlertService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AlertServiceImpl implements AlertService {


    private final AlertRepository alertRepository;
    /**
     * @param alertId 
     * @return
     */
    @Override
    public AlertDto getOneAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert with id [%s] not found ".formatted(alertId)
                ));
        return AlertMapper.AMP.modelToDto(alert);
    }

    /**
     * @return 
     */
    @Override
    public List<AlertDto> getAllAlert() {
        List<Alert> alertList = alertRepository.findAll();
        return AlertMapper.AMP.listModelToDto(alertList);
    }

    /**
     * @param alertDto 
     * @return
     */
    @Override
    public AlertDto createOneAlert(AlertDto alertDto) {
        Alert alert = Alert.builder()
                .alertObject(alertDto.getAlertObject())
                .alertMessage(alertDto.getAlertMessage())
                .alertCode(alertDto.getAlertCode())
                .build();

         alert = alertRepository.save(alert);
        return AlertMapper.AMP.modelToDto(alert);
    }

    /**
     * @param alertId
     * @param alertDto
     * @return
     */
    @Override
    public AlertDto updateOneAlert(Long alertId, AlertDto alertDto) {
        Alert existedAlert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert with id [%s] not found ".formatted(alertId)
                ));
        if (alertDto.getAlertCode() != null){
            existedAlert.setAlertCode(alertDto.getAlertCode());
        }
        if (alertDto.getAlertMessage() != null){
            existedAlert.setAlertMessage(alertDto.getAlertMessage());
        }
        return AlertMapper.AMP.modelToDto(existedAlert);
    }

    /**
     * @param alertId 
     */
    @Override
    public void deleteOneAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert with id [%s] not found ".formatted(alertId)
                ));
        alertRepository.delete(alert);
    }
}
