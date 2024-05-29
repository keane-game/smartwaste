package sonaged.collecte.master.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import sonaged.collecte.master.dto.User;
import sonaged.collecte.master.dto.UserResponse;
import sonaged.collecte.master.model.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {
    UserResponse getOneUser(Long userId);

    List<UserResponse> getAllUser();

    UserResponse createOneUser(User user);

    UserResponse updateOneUser(Long userId, UserResponse userResponse);
    void deleteOneUser(Long userId);

    UserEntity loadUserByUsername(String username);

}
