package sonaged.collecte.master.service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.dto.Authentification;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.model.UserEntity;
import sonaged.collecte.master.model.Validation;
import sonaged.collecte.master.repository.UserRepository;
import sonaged.collecte.master.securite.JwtService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
@Service
public class AuthService   {

    private BCryptPasswordEncoder passwordEncoder;
    private ValidationService validationService;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private UserRepository userRepository;

    public void inscription(UserEntity user) {

        if(!user.getUserEmail().contains("@")) {
            throw  new ResourceNotFoundException("Votre email est invalide");
        }
        if(!user.getUserEmail().contains(".")) {
            throw  new ResourceNotFoundException("Votre email est invalide");
        }

        Optional<UserEntity> utilisateurOptional = this.userRepository.findByUserEmail(user.getUserEmail());
        if(utilisateurOptional.isPresent()) {
            throw  new ResourceNotFoundException ("Votre email est déjà utilisé");
        }
        String mdpCrypte = this.passwordEncoder.encode("Sonaged@123");
        user.setPassword(mdpCrypte);

        user.setAuthority (user.getAuthority());

        user = this.userRepository.save(user);
        this.validationService.enregistrer(user);
    }


    public void activation(Map<String, String> activation) {
        Validation validation = this.validationService.lireEnFonctionDuCode(activation.get("code"));
        if(Instant.now().isAfter(validation.getExpiration())){
            throw  new ResourceNotFoundException("Votre code a expiré");
        }
        UserEntity userActive = this.userRepository.findById(validation.getUser ( ).getUserId ()).orElseThrow(() -> new ResourceNotFoundException("Utilisateur inconnu"));
        userActive.setActivated (true);
        this.userRepository.save(userActive);
    }

    public Map<String, String> connexion(Authentification authentification) {
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

    public List<UserEntity> getAllUser() {
        return (List<UserEntity>) userRepository.findAll ();
    }
}
