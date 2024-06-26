package sonaged.collecte.master.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.Authentification;
import sonaged.collecte.master.model.UserEntity;
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
    public void inscription(@RequestBody UserEntity user) {
        log.info("Inscription");
        this.authService.inscription(user);
    }

    @PostMapping(path = "activation")
    public void activation(@RequestBody Map<String, String> activation) {
        this.authService.activation(activation);
    }

    @PostMapping(path = "connexion")
    public Map<String, String> connexion(@RequestBody Authentification authentification) {
        return authService.connexion (authentification);
    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/users")
    public List<UserEntity> getUsers(){
        return authService.getAllUser();
    }
}
