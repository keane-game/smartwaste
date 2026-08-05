package sn.smartwaste.collect.identity.application.service;
import sn.smartwaste.collect.identity.application.dto.Authentification;
import sn.smartwaste.collect.identity.application.dto.User;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.identity.domain.model.UserEntity;

import java.util.List;
import java.util.Map;

public interface AuthService   {

    void register(User user);

    void activation(String code);

    Map<String, String> authentication(Authentification authentification);

    UserEntity loadUserByUsername(String username) throws ResourceNotFoundException;

    //List<User> getAllUser();

}
