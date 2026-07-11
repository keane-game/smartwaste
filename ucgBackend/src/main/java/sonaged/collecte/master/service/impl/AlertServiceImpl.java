package sonaged.collecte.master.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sonaged.collecte.master.dto.Image;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.mapper.ImageMapper;
import sonaged.collecte.master.model.AlertEntity;
import sonaged.collecte.master.model.ImageEntity;
import sonaged.collecte.master.repository.AlertRepository;
import sonaged.collecte.master.dto.Alert;
import sonaged.collecte.master.mapper.AlertMapper;
import sonaged.collecte.master.service.AlertService;

import java.io.DataInput;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class AlertServiceImpl implements AlertService {


    private static final Logger log = LoggerFactory.getLogger (AlertServiceImpl.class);
    private final AlertRepository alertRepository;
    private final ObjectMapper mapper;

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
        var alertList = alertRepository.findAll();
        return AlertMapper.AMP.asListDto(alertList);
    }

    @Override
    @Transactional
    public Page<Alert> readAllAlert(Pageable pageable){
        return alertRepository.findAll (pageable).map (AlertMapper.AMP::asDto);
    }


    @Override
    public Alert createAlert(Alert alert, MultipartFile file) throws IOException {
        // Extract user details and file from userRequest

        if(!file.isEmpty ( )){
            alert.setImage (getImageData(file));
        }
        log.info ("alert {}",alert);
        var savedAlert = alertRepository.save(AlertMapper.AMP.asModel(alert));
        return AlertMapper.AMP.asDto (savedAlert);
    }

    @Override
    public Alert createAlert(String alert, MultipartFile file) throws IOException {

        // Mapping object String to alert dto
        var alertMapper = mapper.readValue (alert, Alert.class);

        log.info ("file {}", alertMapper.getCoordinate().getLatitude());
        if(file != null){
            alertMapper.setImage (getImageData(file));
        }

        if (alertMapper.getCoordinate() == null ||
                Objects.equals(alertMapper.getCoordinate().getLatitude(), "") ||
                Objects.equals(alertMapper.getCoordinate().getLongitude(), "")) {
            alertMapper.setCoordinate(null);
        }
        var savedAlert = alertRepository.save(AlertMapper.AMP.asModel(alertMapper));
        return AlertMapper.AMP.asDto (savedAlert);
    }

    @Override
    public Alert createAlertFile(Alert alert) throws IOException {
        // Extract user details and file from userRequest

        //var image = getImageData(alert.getFile ( ));

        var alerts = new AlertEntity (  );
        alerts.setDisplayPicture (alert.getFile ().getBytes ());
        alerts.setObject (alert.getObject ( ));
        alerts.setCode (alert.getCode ());
        alerts.setMessage (alert.getMessage ( ));
        // log.info ("image {}",image);
        log.info ("alerts {}",alert);
        return AlertMapper.AMP.asDto (alertRepository.save(alerts));
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
        if(file != null){
            existedAlert.setImage ( ImageMapper.IMG.asModel(getImageData(file)));
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
        alertRepository.delete(alert);
    }


    public Image getImageData(MultipartFile file) throws IOException {
        var image = new Image();
        image.setName(file.getOriginalFilename());
        image.setType(file.getContentType());
        image.setData(file.getBytes());
        return image;
    }
}