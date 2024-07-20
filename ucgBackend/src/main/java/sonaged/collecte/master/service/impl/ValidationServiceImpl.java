package sonaged.collecte.master.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.model.UserEntity;
import sonaged.collecte.master.model.Validation;
import sonaged.collecte.master.repository.ValidationRepository;
import sonaged.collecte.master.service.NotificationService;
import sonaged.collecte.master.service.ValidationService;

import java.time.Instant;
import java.util.Random;
@AllArgsConstructor
@Service
public class ValidationServiceImpl implements ValidationService {

    private ValidationRepository validationRepository;
    private NotificationService notificationService;

    public void registerUserCode(UserEntity user) {
        Validation validation = new Validation();
        validation.setUser (user);
        Instant creation = Instant.now();
        validation.setCreation(creation);
        Instant expiration = creation.plusSeconds(84_000_000);
        validation.setExpiration(expiration);
        Random random = new Random();
        int randomInteger = random.nextInt(999999);
        String code = String.format("%06d", randomInteger);

        validation.setCode(code);
        this.validationRepository.save(validation);
        this.notificationService.sendCodeOfValidation(validation);
    }

    public Validation readByCode(String code) {
        return this.validationRepository.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Votre code est invalide"));
    }
}
