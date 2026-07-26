package sonaged.collecte.master.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.dto.Authentification;
import sonaged.collecte.master.dto.User;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.mapper.UserMapper;
import sonaged.collecte.master.model.UserEntity;
import sonaged.collecte.master.model.Validation;
import sonaged.collecte.master.repository.UserRepository;
import sonaged.collecte.master.security.JwtService;
import sonaged.collecte.master.service.AuthService;
import sonaged.collecte.master.service.ValidationService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private BCryptPasswordEncoder passwordEncoder;
    private ValidationService validationService;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private UserRepository userRepository;

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

        user.setAuthority (user.getAuthority());
        user.setActivated(false);

        var userEntity = this.userRepository.save(UserMapper.UMP.asModel(user));
        this.validationService.registerUserCode(userEntity);
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
            return this.jwtService.generate(authentification.username());
        }
        return null;
    }

    public UserEntity loadUserByUsername(String username) throws ResourceNotFoundException {
        return this.userRepository
                .findByUserEmail (username)
                .orElseThrow(() -> new  ResourceNotFoundException("Email ou mot de passe incorrect!"));
    }



}
