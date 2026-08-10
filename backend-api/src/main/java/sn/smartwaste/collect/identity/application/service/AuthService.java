package sn.smartwaste.collect.identity.application.service;
import java.util.UUID;

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

    /**
     * Changement de mot de passe self-service (ADR-0021, pont avant Keycloak).
     *
     * <p>Révoque ensuite toutes les sessions de l'utilisateur, y compris celle de l'appel courant —
     * un changement de mot de passe déconnecte partout, l'utilisateur se reconnecte avec le nouveau.
     *
     * @throws ResourceNotFoundException si {@code currentPassword} ne correspond pas au mot de passe
     *         actuel, ou si {@code newPassword} est absent
     */
    void changePassword(UUID userId, String currentPassword, String newPassword);

    //List<User> getAllUser();

}
