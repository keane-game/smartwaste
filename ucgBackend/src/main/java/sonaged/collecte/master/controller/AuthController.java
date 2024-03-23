package sonaged.collecte.master.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.AuthentificationDTO;
import sonaged.collecte.master.model.User;
import sonaged.collecte.master.model.Utilisateur;
import sonaged.collecte.master.securite.JwtService;
import sonaged.collecte.master.service.AuthService;

import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("api/")
public class AuthController {

    private AuthService authService;

    @PostMapping(path = "inscription")
    public void inscription(@RequestBody User user) {
        log.info("Inscription");
        this.authService.inscription(user);
    }

    @PostMapping(path = "activation")
    public void activation(@RequestBody Map<String, String> activation) {
        this.authService.activation(activation);
    }

    @PostMapping(path = "connexion")
    public Map<String, String> connexion(@RequestBody AuthentificationDTO authentificationDTO) {
        return authService.connexion (authentificationDTO);
    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/api/user")
    public List<User> getUsers(){
        return authService.getAllUser();
    }
}
