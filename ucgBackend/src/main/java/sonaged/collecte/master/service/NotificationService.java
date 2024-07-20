package sonaged.collecte.master.service;

import sonaged.collecte.master.model.Validation;

public interface NotificationService {

    void sendCodeOfValidation(Validation validation);
}
