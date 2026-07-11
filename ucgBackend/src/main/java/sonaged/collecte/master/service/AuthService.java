package sonaged.collecte.master.service;
import sonaged.collecte.master.dto.Authentification;
import sonaged.collecte.master.dto.User;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.model.UserEntity;

import java.util.List;
import java.util.Map;

public interface AuthService   {

    void register(User user);

    void activation(String code);

    Map<String, String> authentication(Authentification authentification);

    UserEntity loadUserByUsername(String username) throws ResourceNotFoundException;

    //List<User> getAllUser();

}
