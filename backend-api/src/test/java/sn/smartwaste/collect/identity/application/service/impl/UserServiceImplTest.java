package sn.smartwaste.collect.identity.application.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import sn.smartwaste.collect.identity.application.dto.User;
import sn.smartwaste.collect.identity.domain.model.UserEntity;
import sn.smartwaste.collect.identity.domain.repository.AuthorityRepository;
import sn.smartwaste.collect.identity.domain.repository.UserRepository;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;

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
    @DisplayName("un compte sans mot de passe est refusé plutôt que créé avec un secret connu")
    void missingPasswordIsRejected() {
        // Sans ce refus, retirer la constante aurait produit pire : un mot de passe nul en base.
        assertThatThrownBy(() -> userService.createUser(submitted(null)))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> userService.createUser(submitted("   ")))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any(UserEntity.class));
    }
}
