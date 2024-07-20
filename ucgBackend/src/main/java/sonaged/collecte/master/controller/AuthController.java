package sonaged.collecte.master.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.Authentification;
import sonaged.collecte.master.dto.User;
import sonaged.collecte.master.dto.reponse.ActivationCode;
import sonaged.collecte.master.service.AuthService;

import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("auth/")
public class AuthController {

    private AuthService authService;

    @PostMapping(path = "register")
    public void register(@RequestBody User user) {
        log.info("register");
        this.authService.register(user);
    }

    @PostMapping(path = "activation")
    public void activation(@RequestBody ActivationCode code) {
        this.authService.activation(code.getCode());
    }

    @PostMapping(path = "authenticate")
    public Map<String, String> authenticate(@RequestBody Authentification authentification) {
        return authService.authentication (authentification);
    }


}
