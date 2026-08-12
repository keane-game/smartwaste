package sn.smartwaste.collect.identity.application.service.impl;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import sn.smartwaste.collect.identity.application.dto.User;
import sn.smartwaste.collect.identity.application.service.SessionService;
import sn.smartwaste.collect.identity.domain.model.UserEntity;
import sn.smartwaste.collect.identity.domain.repository.AuthorityRepository;
import sn.smartwaste.collect.identity.domain.repository.UserRepository;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Création d'un compte par l'administration.
 *
 * <p><b>Le défaut fermé ici.</b> {@code createUser} encodait la constante {@code "Sonaged@123"}
 * pour tous les comptes créés par ce chemin. La valeur est en clair dans le dépôt et dans son
 * historique : connaître l'adresse d'un collègue suffisait donc à entrer dans son compte.
 * {@code AuthServiceImpl.register} avait été corrigé ; celui-ci, réservé à l'administration, était
 * resté en arrière — et comme il attribue aussi le rôle, c'était le plus intéressant des deux à
 * emprunter.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthorityRepository authorityRepository;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private SessionService sessionService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    private static User submitted(String password) {
        var user = new User();
        user.setUserEmail("agent@sonaged.sn");
        user.setUserPassword(password);
        return user;
    }

    @Test
    @DisplayName("le mot de passe fourni est haché, pas remplacé par une constante partagée")
    void submittedPasswordIsHashed() {
        when(bCryptPasswordEncoder.encode("MonMotDePasse!42")).thenReturn("$2a$10$hash");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        userService.createUser(submitted("MonMotDePasse!42"));

        ArgumentCaptor<UserEntity> saved = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getUserPassword()).isEqualTo("$2a$10$hash");
        // Le point du test : l'ancienne constante ne doit plus jamais être encodée.
        verify(bCryptPasswordEncoder, never()).encode("Sonaged@123");
    }

    @Test
    @DisplayName("un identifiant fourni par l'appelant est ignoré : jamais un UPDATE déguisé en INSERT")
    void submittedUserIdIsIgnored() {
        when(bCryptPasswordEncoder.encode(any())).thenReturn("$2a$10$hash");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        // L'id d'un compte EXISTANT quelconque, y compris potentiellement un SUPER_ADMIN : sans
        // le correctif, un ADMIN pourrait ainsi écraser ce compte au lieu d'en créer un nouveau.
        User request = submitted("MonMotDePasse!42");
        request.setUserId(UUID.randomUUID());

        userService.createUser(request);

        ArgumentCaptor<UserEntity> saved = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getUserId()).isNull();
    }

    @Test
    @DisplayName("un compte sans mot de passe est refusé plutôt que créé avec un secret connu")
    void missingPasswordIsRejected() {
        // Sans ce refus, retirer la constante aurait produit pire : un mot de passe nul en base.
        assertThatThrownBy(() -> userService.createUser(submitted(null)))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> userService.createUser(submitted("   ")))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("desactiver un compte le marque inactif et ferme toutes ses sessions ouvertes")
    void deactivateUserClosesAllSessions() {
        UUID userId = UUID.randomUUID();
        UserEntity existing = new UserEntity();
        existing.setUserId(userId);
        existing.setActivated(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        userService.deactivateUser(userId);

        ArgumentCaptor<UserEntity> saved = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().isActivated()).isFalse();
        // Le point du correctif (audit 2026-08-09, ADR-0021) : revokeAllForUser n'avait jusqu'ici
        // aucun appelant — une desactivation qui n'y ferait pas appel laisserait les jetons deja
        // emis fonctionner jusqu'a leur expiration.
        verify(sessionService).revokeAllForUser(userId);
    }

    @Test
    @DisplayName("reactiver un compte inconnu echoue plutot que de silencieusement ne rien faire")
    void activateUnknownUserFails() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.activateUser(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("supprimer un compte le marque en PENDING_DELETION et ferme ses sessions, sans le supprimer vraiment")
    void deleteUserIsSoftDelete() {
        // Defaut releve par audit (2026-08-10) : User etait le seul repository du projet a
        // supprimer reellement ses lignes, contrairement au reste du referentiel.
        UUID userId = UUID.randomUUID();
        UserEntity existing = new UserEntity();
        existing.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        userService.deleteUser(userId);

        org.mockito.ArgumentCaptor<UserEntity> saved = org.mockito.ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().isPendingDeletion()).isTrue();
        verify(userRepository, never()).delete(any(UserEntity.class));
        verify(sessionService).revokeAllForUser(userId);
    }

    @Test
    @DisplayName("la recherche delegue au repository et mappe le resultat en DTO")
    void searchUserDelegatesToRepository() {
        UserEntity entity = new UserEntity();
        entity.setUserId(UUID.randomUUID());
        entity.setUserEmail("awa@example.sn");
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        when(userRepository.search("awa", pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(entity)));

        var result = userService.searchUser("awa", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserEmail()).isEqualTo("awa@example.sn");
    }
}
