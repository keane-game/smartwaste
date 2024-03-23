package sonaged.collecte.master.service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.LogManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import sonaged.collecte.master.dto.AuthentificationDTO;
import sonaged.collecte.master.dto.UserDto;
import sonaged.collecte.master.enums.Permission;
import sonaged.collecte.master.model.Role;
import sonaged.collecte.master.model.User;
import sonaged.collecte.master.model.Utilisateur;
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

    public void inscription(User user) {

        if(!user.getUserEmail().contains("@")) {
            throw  new RuntimeException("Votre mail invalide");
        }
        if(!user.getUserEmail().contains(".")) {
            throw  new RuntimeException("Votre mail invalide");
        }

        Optional<User> utilisateurOptional = this.userRepository.findByUserEmail(user.getUserEmail());
        if(utilisateurOptional.isPresent()) {
            throw  new RuntimeException("Votre mail est déjà utilisé");
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
            throw  new RuntimeException("Votre code a expiré");
        }
        User userActive = this.userRepository.findById(validation.getUser ( ).getUserId ()).orElseThrow(() -> new RuntimeException("Utilisateur inconnu"));
        userActive.setActivated (true);
        this.userRepository.save(userActive);
    }

    public Map<String, String> connexion(AuthentificationDTO authentificationDTO) {
        log.info("connexion");
        final Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken (authentificationDTO.username(), authentificationDTO.password())
        );
        log.info("connexion");
        if(authenticate.isAuthenticated()) {
            return this.jwtService.generate(authentificationDTO.username());
        }
        return null;
    }

    public List<User> getAllUser() {
        return (List<User>) userRepository.findAll ();
    }
}
