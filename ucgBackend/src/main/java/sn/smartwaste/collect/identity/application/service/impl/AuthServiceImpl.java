package sn.smartwaste.collect.identity.application.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import sn.smartwaste.collect.identity.application.dto.Authentification;
import sn.smartwaste.collect.identity.application.dto.User;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.identity.application.mapper.UserMapper;
import sn.smartwaste.collect.identity.domain.model.AuthorityEntity;
import sn.smartwaste.collect.identity.domain.model.UserEntity;
import sn.smartwaste.collect.identity.domain.model.Validation;
import sn.smartwaste.collect.identity.domain.repository.AuthorityRepository;
import sn.smartwaste.collect.identity.domain.repository.UserRepository;
import sn.smartwaste.collect.identity.infrastructure.security.JwtService;
import sn.smartwaste.collect.identity.application.service.AuthService;
import sn.smartwaste.collect.identity.application.service.SessionService;
import sn.smartwaste.collect.identity.application.service.ValidationService;
import sn.smartwaste.collect.shared.domain.model.DeletionStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    /**
     * Rôle attribué d'office à tout compte créé par l'inscription publique.
     *
     * <p>Semé par le changelog {@code 2.1.0_identity_uuid.xml} — c'est le rôle le moins privilégié
     * des trois rôles de référence.
     */
    private static final String DEFAULT_REGISTRATION_ROLE = "USER";

    private BCryptPasswordEncoder passwordEncoder;
    private ValidationService validationService;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private UserRepository userRepository;
    private AuthorityRepository authorityRepository;
    private SessionService sessionService;

    public void register(User user) {

        if(!user.getUserEmail().contains("@")) {
            throw  new ResourceNotFoundException("Votre email est invalide");
        }
        if(!user.getUserEmail().contains(".")) {
            throw  new ResourceNotFoundException("Votre email est invalide");
        }

        Optional<UserEntity> userOptional = this.userRepository.findByUserEmail(user.getUserEmail());
        if(userOptional.isPresent()) {
            throw  new ResourceNotFoundException ("Votre email est déjà utilisé");
        }
        if (user.getUserPassword() == null || user.getUserPassword().isBlank()) {
            throw new ResourceNotFoundException("Le mot de passe est obligatoire");
        }
        String pwdCrypt = this.passwordEncoder.encode(user.getUserPassword());
        user.setUserPassword(pwdCrypt);

        user.setActivated(false);

        UserEntity userToCreate = UserMapper.UMP.asModel(user);

        // ---------------------------------------------------------------------------------
        // Élévation de privilèges refermée.
        //
        // `/auth/register` est en `permitAll`, et l'ancien code faisait
        // `user.setAuthority(user.getAuthority())` : le rôle arrivait donc **du corps de la
        // requête**. N'importe qui pouvait s'inscrire avec l'identifiant du rôle SUPER_ADMIN,
        // recevoir le code d'activation à sa propre adresse, activer le compte, et se retrouver
        // administrateur. Le rôle est désormais imposé par le serveur et ce qui vient du client
        // est écrasé — c'est fait **après** le mapping DTO -> entité, pour qu'aucune valeur
        // fournie par l'appelant ne puisse survivre.
        //
        // Effet de bord attendu : cela répare aussi l'inscription, jusqu'ici cassée. Le
        // formulaire Angular n'envoie aucun `authority` alors que la colonne est `nullable=false`,
        // donc tout signup légitime échouait sur une violation de contrainte.
        // ---------------------------------------------------------------------------------
        userToCreate.setAuthority(defaultRegistrationAuthority());

        var userEntity = this.userRepository.save(userToCreate);
        this.validationService.registerUserCode(userEntity);
    }

    private AuthorityEntity defaultRegistrationAuthority() {
        return this.authorityRepository
                .findByNameAndDeletionStatus(DEFAULT_REGISTRATION_ROLE, DeletionStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException(
                        "Rôle par défaut « %s » introuvable : les rôles de référence ne sont pas semés"
                                .formatted(DEFAULT_REGISTRATION_ROLE)));
    }

    public void activation(String code) {
        Validation validation = this.validationService.readByCode(code);
        if(Instant.now().isAfter(validation.getExpiration())){
            throw  new ResourceNotFoundException("Votre code a expiré");
        }
        UserEntity userActive = this.userRepository.findById(validation.getUser ( ).getUserId ())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur inconnu"));
        userActive.setActivated (true);
        this.userRepository.save(userActive);
    }

    public Map<String, String> authentication(Authentification authentification) {
        log.info("connexion");
        final Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken (authentification.username(), authentification.password())
        );
        log.info("connexion");
        if(authenticate.isAuthenticated()) {
            UserEntity user = loadUserByUsername(authentification.username());
            var tokens = this.sessionService.openSession(user);
            // Forme de réponse conservée (`bearer`) pour ne pas casser les clients existants ;
            // `refresh` est purement additif.
            return Map.of("bearer", tokens.bearer(), "refresh", tokens.refresh());
        }
        return null;
    }

    public UserEntity loadUserByUsername(String username) throws ResourceNotFoundException {
        return this.userRepository
                .findByUserEmail (username)
                .orElseThrow(() -> new  ResourceNotFoundException("Email ou mot de passe incorrect!"));
    }



}
