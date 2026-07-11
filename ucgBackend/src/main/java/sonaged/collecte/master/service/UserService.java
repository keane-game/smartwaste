package sonaged.collecte.master.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import sonaged.collecte.master.dto.User;
import sonaged.collecte.master.model.UserEntity;

import java.util.List;

public interface UserService extends UserDetailsService {
    User readUser(Long userId);

    List<User> readAllUser();

    User createUser(User user);

    User updateUser(Long userId, User user);

    void deleteUser(Long userId);

    UserEntity loadUserByUsername(String username);

    Page<User> readAllUser(Pageable pageable);

}
