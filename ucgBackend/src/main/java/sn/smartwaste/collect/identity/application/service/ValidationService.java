package sn.smartwaste.collect.identity.application.service;

import sn.smartwaste.collect.identity.domain.model.UserEntity;
import sn.smartwaste.collect.identity.domain.model.Validation;

public interface ValidationService {

    void registerUserCode(UserEntity user);

    Validation readByCode(String code);
}
