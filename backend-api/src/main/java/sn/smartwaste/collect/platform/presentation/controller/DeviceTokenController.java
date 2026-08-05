package sn.smartwaste.collect.platform.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.platform.application.service.PushNotificationService;

/**
 * Appareils joignables d'un citoyen (`/v1/device-tokens`, G2 du backlog).
 *
 * <p>Le jeton appartient à l'appareil et le compte à celui qui est connecté : l'identité de
 * l'utilisateur n'est <b>jamais</b> lue depuis le corps de la requête. C'est la leçon de
 * {@code /auth/register}, où le rôle arrivait du client et permettait de s'inscrire administrateur.
 */
@RestController
@RequestMapping("/v1/device-tokens")
public class DeviceTokenController {

    private final PushNotificationService pushNotificationService;
    private final CurrentUserProvider currentUserProvider;

    public DeviceTokenController(PushNotificationService pushNotificationService,
                                 CurrentUserProvider currentUserProvider) {
        this.pushNotificationService = pushNotificationService;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "Enregistrer l'appareil courant",
               description = "Idempotent : un jeton deja connu est rattache, jamais duplique.")
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void register(@RequestBody DeviceRegistration body) {
        pushNotificationService.register(currentUserProvider.requireCurrentUserId(),
                requireToken(body), body.platform());
    }

    @Operation(summary = "Ne plus etre joint sur cet appareil")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void revoke(@RequestBody DeviceRegistration body) {
        // Seul le proprietaire du jeton peut le revoquer : le service verifie le rattachement,
        // sans quoi connaitre un jeton suffirait a desabonner quelqu'un d'autre.
        pushNotificationService.revoke(currentUserProvider.requireCurrentUserId(), requireToken(body));
    }

    private String requireToken(DeviceRegistration body) {
        if (body == null || body.token() == null || body.token().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le jeton est obligatoire");
        }
        return body.token();
    }

    /** @param platform {@code ANDROID}, {@code IOS} ou {@code WEB} — informatif */
    public record DeviceRegistration(String token, String platform) { }
}
