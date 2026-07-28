package sn.smartwaste.collect.identity.application.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import sn.smartwaste.collect.identity.application.dto.User;
import sn.smartwaste.collect.identity.domain.model.UserEntity;

import java.util.List;

public interface UserService extends UserDetailsService {
    User readUser(UUID userId);

    List<User> readAllUser();

    User createUser(User user);

    User updateUser(UUID userId, User user);

    void deleteUser(UUID userId);

    UserEntity loadUserByUsername(String username);

    Page<User> readAllUser(Pageable pageable);

}
