package sn.smartwaste.collect.identity.presentation.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.identity.application.dto.Authentification;
import sn.smartwaste.collect.identity.application.dto.User;
import sn.smartwaste.collect.identity.application.dto.ActivationCode;
import sn.smartwaste.collect.identity.application.dto.AuthTokens;
import sn.smartwaste.collect.identity.application.service.AuthService;
import sn.smartwaste.collect.identity.application.service.PasswordResetService;
import sn.smartwaste.collect.identity.application.service.SessionService;
import sn.smartwaste.collect.identity.application.service.UserService;
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
    private PasswordResetService passwordResetService;
    private CurrentUserProvider currentUserProvider;
    private UserService userService;

    @PostMapping(path = "register")
    @ResponseStatus(HttpStatus.CREATED)
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

    /**
     * Profil du compte authentifié — le « qui suis-je » qui manquait.
     *
     * <p>Sans lui, un frontend n'avait aucun moyen fiable de savoir qui venait de se connecter
     * autrement qu'en décodant lui-même le JWT (dont le claim {@code role} est explicitement
     * documenté comme informatif, cf. {@code JwtService}). Aucune règle dédiée dans
     * {@code SecurityConfiguration} : {@code GET /auth/**} n'est jamais rendu public, donc ce
     * chemin retombe sur {@code anyRequest().authenticated()} — un jeton valide suffit et est exigé.
     */
    @GetMapping(path = "me")
    public User me() {
        return userService.readUser(currentUserProvider.requireCurrentUserId());
    }

    /**
     * Change le mot de passe du compte authentifié.
     *
     * <p>Sous {@code /auth/**}, mais protégé explicitement dans {@code SecurityConfiguration} (règle
     * posée avant le {@code permitAll} général du préfixe) : contrairement au reste de ce
     * controller, cette opération exige une identité — c'est un geste sur SON PROPRE compte, jamais
     * sur celui d'un tiers désigné par le corps de la requête.
     */
    @PostMapping(path = "change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody ChangePasswordRequest request) {
        authService.changePassword(currentUserProvider.requireCurrentUserId(),
                request.currentPassword(), request.newPassword());
    }

    /** Corps de {@code POST /auth/change-password}. */
    public record ChangePasswordRequest(String currentPassword, String newPassword) { }

    /**
     * Déclenche l'envoi d'un jeton de réinitialisation si l'adresse correspond à un compte.
     *
     * <p>Répond toujours 204 : que l'e-mail existe ou non, un appelant anonyme ne doit pas pouvoir
     * énumérer les comptes en observant la réponse.
     */
    @PostMapping(path = "password-reset/request")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestPasswordReset(@RequestBody PasswordResetRequestBody request) {
        passwordResetService.requestReset(request.email());
    }

    /** Corps de {@code POST /auth/password-reset/request}. */
    public record PasswordResetRequestBody(String email) { }

    /** Consomme un jeton de réinitialisation et applique le nouveau mot de passe. */
    @PostMapping(path = "password-reset/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmPasswordReset(@RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.confirmReset(request.token(), request.newPassword());
    }

    /** Corps de {@code POST /auth/password-reset/confirm}. */
    public record PasswordResetConfirmRequest(String token, String newPassword) { }

}
