package sn.smartwaste.collect.waste.application.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.waste.application.mapper.ImageMapper;
import sn.smartwaste.collect.waste.domain.model.AlertEntity;
import sn.smartwaste.collect.waste.domain.repository.AlertRepository;
import sn.smartwaste.collect.waste.application.dto.Alert;
import sn.smartwaste.collect.waste.application.dto.Image;
import sn.smartwaste.collect.waste.application.mapper.AlertMapper;
import sn.smartwaste.collect.waste.application.service.AlertService;
import sn.smartwaste.collect.waste.application.service.ImageService;
import sn.smartwaste.collect.shared.domain.event.AlertRaisedEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class AlertServiceImpl implements AlertService {


    private static final Logger log = LoggerFactory.getLogger (AlertServiceImpl.class);
    private final AlertRepository alertRepository;
    private final ObjectMapper mapper;
    private final ImageService imageService;
    // P2-1 / ADR-0007 : publication de l'événement « alerte levée » → diffusion SSE.
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Alert readAlert(Long alertId) {
        var alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert with id [%s] not found ".formatted(alertId)
                ));
        return AlertMapper.AMP.asDto(alert);
    }


    @Override
    public List<Alert> readAllAlert() {
        var alertList = alertRepository.findByDeletionStatus(sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE);
        return AlertMapper.AMP.asListDto(alertList);
    }

    @Override
    @Transactional
    public Page<Alert> readAllAlert(Pageable pageable){
        return alertRepository.findByDeletionStatus (sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE, pageable).map (AlertMapper.AMP::asDto);
    }


    @Override
    public Alert createAlert(Alert alert, MultipartFile file) throws IOException {
        // Extract user details and file from userRequest

        if(file != null && !file.isEmpty ( )){
            alert.setImage (imageService.store(file));
        }
        log.info ("alert {}",alert);
        var savedAlert = alertRepository.save(AlertMapper.AMP.asModel(alert));
        return publishRaised(savedAlert);
    }

    @Override
    public Alert createAlert(String alert, MultipartFile file) throws IOException {

        // Mapping object String to alert dto
        var alertMapper = mapper.readValue (alert, Alert.class);

        log.info ("file {}", alertMapper.getCoordinate().getLatitude());
        if(file != null && !file.isEmpty()){
            alertMapper.setImage (imageService.store(file));
        }

        if (alertMapper.getCoordinate() == null ||
                Objects.equals(alertMapper.getCoordinate().getLatitude(), "") ||
                Objects.equals(alertMapper.getCoordinate().getLongitude(), "")) {
            alertMapper.setCoordinate(null);
        }
        var savedAlert = alertRepository.save(AlertMapper.AMP.asModel(alertMapper));
        return publishRaised(savedAlert);
    }

    @Override
    public Alert createAlertFile(Alert alert) throws IOException {
        // Extract user details and file from userRequest

        var alerts = new AlertEntity (  );
        if (alert.getFile () != null && !alert.getFile ().isEmpty ()) {
            alerts.setImage (ImageMapper.IMG.asModel (imageService.store (alert.getFile ())));
        }
        alerts.setObject (alert.getObject ( ));
        alerts.setCode (alert.getCode ());
        alerts.setMessage (alert.getMessage ( ));
        log.info ("alerts {}",alert);
        return publishRaised(alertRepository.save(alerts));
    }

    @Override
    @Transactional
    public Alert updateAlert(Long alertId, String alert, MultipartFile file) throws IOException {
        // Mapping object String to alert dto
        var alertMapper = mapper.readValue (alert, Alert.class);

        var existedAlert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert with id [%s] not found ".formatted(alertId)
                ));
        if (alertMapper.getCode() != null){
            existedAlert.setCode(alertMapper.getCode());
        }
        log.info("file error {}", file);
        if(file != null && !file.isEmpty()){
            existedAlert.setImage ( ImageMapper.IMG.asModel(imageService.store(file)));
        }
        if (alertMapper.getMessage() != null){
            existedAlert.setMessage(alertMapper.getMessage());
        }
        if (alertMapper.getObject() != null){
            existedAlert.setObject(alertMapper.getObject());
        }
        return AlertMapper.AMP.asDto(alertRepository.save(existedAlert));
    }


    @Override
    public void deleteAlert(Long alertId) {
        var alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert with id [%s] not found ".formatted(alertId)
                ));
        alert.markForDeletion(java.time.LocalDateTime.now()); alertRepository.save(alert); // soft-delete (rétention + purge planifiée)
    }

    /**
     * Mappe l'alerte enregistrée en DTO et publie {@link AlertRaisedEvent} (P2-1 / ADR-0007).
     *
     * <p>La diffusion SSE est branchée sur cet événement en phase AFTER_COMMIT : le superviseur
     * ne voit jamais une alerte dont la transaction serait annulée.
     */
    private Alert publishRaised(AlertEntity savedAlert) {
        Alert dto = AlertMapper.AMP.asDto(savedAlert);
        eventPublisher.publishEvent(AlertRaisedEvent.manual(toEventPayload(dto)));
        return dto;
    }

    /**
     * Traduit le DTO de sortie en charge utile d'événement.
     *
     * <p>Cette conversion est le prix — assumé — de la frontière : l'événement vit dans le shared
     * kernel et ne peut pas référencer un type du contexte « Déchets » (voir
     * {@link AlertRaisedEvent}). En échange, les abonnés ne dépendent plus de ce contexte, et le
     * jour où l'événement partira sur un broker il est déjà sérialisable tel quel.
     */
    private static AlertRaisedEvent.RaisedAlert toEventPayload(Alert dto) {
        Image image = dto.getImage();
        return new AlertRaisedEvent.RaisedAlert(
                dto.getAlertId(),
                dto.getObject(),
                dto.getMessage(),
                dto.getAddress(),
                dto.getCode() == null ? null : dto.getCode().name(),
                image == null ? null : new AlertRaisedEvent.ImageRef(image.getUrl(), image.getName()));
    }
}
