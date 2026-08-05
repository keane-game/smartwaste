package sn.smartwaste.collect.identity.application.service.impl;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.identity.application.api.UserDirectory;
import sn.smartwaste.collect.identity.domain.repository.UserRepository;
import sn.smartwaste.collect.identity.domain.model.UserEntity;

/**
 * Implémentation des contrats publiés par le module « Identité &amp; Accès »
 * ({@code identity.application.api}).
 *
 * <p>Unique endroit où le principal Spring Security est déballé en {@link UserEntity} : le cast
 * était auparavant dupliqué dans les contextes appelants, ce qui leur imposait de connaître
 * l'entité JPA de l'identité.
 */
@Service
public class IdentityApiAdapter implements CurrentUserProvider, UserDirectory {

    private final UserRepository userRepository;

    public IdentityApiAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UUID requireCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserEntity user)) {
            // Ne peut survenir que si l'endpoint appelant a été ouvert dans SecurityConfiguration
            // sans que l'appelant s'en aperçoive : mieux vaut échouer que rattacher la donnée à
            // un utilisateur arbitraire.
            throw new IllegalStateException("Aucun utilisateur authentifié sur la requête courante");
        }
        return user.getUserId();
    }

    @Override
    public boolean currentUserHasAnyRole(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        var portees = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();
        for (String role : roles) {
            // `UserEntity.getAuthorities()` pose deja le prefixe ROLE_ ; on accepte les deux
            // ecritures pour que l'appelant n'ait pas a connaitre cette convention.
            if (portees.contains("ROLE_" + role) || portees.contains(role)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<String> emailOf(UUID userId) {
        return userRepository.findById(userId).map(UserEntity::getUserEmail);
    }
}
