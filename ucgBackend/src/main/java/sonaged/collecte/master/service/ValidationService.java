package sonaged.collecte.master.service;

import sonaged.collecte.master.model.UserEntity;
import sonaged.collecte.master.model.Validation;

public interface ValidationService {

    void registerUserCode(UserEntity user);

    Validation readByCode(String code);
}
