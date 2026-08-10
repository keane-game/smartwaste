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

    /** Réactive un compte désactivé. N'ouvre aucune session — l'utilisateur se reconnecte normalement. */
    User activateUser(UUID userId);

    /**
     * Désactive un compte et ferme immédiatement toutes ses sessions ouvertes
     * ({@link sn.smartwaste.collect.identity.application.service.SessionService#revokeAllForUser}) —
     * une désactivation qui laisserait un jeton d'accès existant continuer à fonctionner jusqu'à son
     * expiration serait incomplète.
     */
    User deactivateUser(UUID userId);

    UserEntity loadUserByUsername(String username);

    Page<User> readAllUser(Pageable pageable);

}
