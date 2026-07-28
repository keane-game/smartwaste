package sn.smartwaste.collect.identity.presentation.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.smartwaste.collect.identity.application.dto.Authentification;
import sn.smartwaste.collect.identity.application.dto.User;
import sn.smartwaste.collect.identity.application.dto.ActivationCode;
import sn.smartwaste.collect.identity.application.dto.AuthTokens;
import sn.smartwaste.collect.identity.application.service.AuthService;
import sn.smartwaste.collect.identity.application.service.SessionService;
import sn.smartwaste.collect.identity.infrastructure.security.JwtService;

import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("auth/")
public class AuthController {

    private AuthService authService;
    private SessionService sessionService;
    private JwtService jwtService;

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

    /**
     * Échange un jeton de rafraîchissement contre un nouveau couple de jetons.
     *
     * <p>Sous {@code /auth/**}, donc en accès public : c'est nécessaire, puisque le jeton d'accès
     * est justement susceptible d'être expiré au moment de l'appel. La preuve d'identité, ici,
     * c'est le jeton de rafraîchissement lui-même.
     */
    @PostMapping(path = "refresh")
    public AuthTokens refresh(@RequestBody RefreshRequest request) {
        return sessionService.refresh(request.refresh());
    }

    /**
     * Ferme la session portée par le jeton d'accès présenté.
     *
     * <p>C'est la déconnexion qui n'existait pas : le client se contentait de vider son stockage
     * local, le jeton restant valide côté serveur jusqu'à son expiration.
     */
    @PostMapping(path = "logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return; // rien à fermer : idempotent, jamais une erreur
        }
        try {
            sessionService.revoke(jwtService.extractSessionId(authorization.substring(7)));
        } catch (RuntimeException ignored) {
            // Jeton illisible : il n'ouvre aucune session, la déconnexion est déjà effective.
            // Répondre 400 renseignerait un appelant anonyme sur la validité d'un jeton.
        }
    }

    /** Corps de {@code POST /auth/refresh}. */
    public record RefreshRequest(String refresh) { }


}
