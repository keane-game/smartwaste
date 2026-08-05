package sn.smartwaste.collect.platform.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import sn.smartwaste.collect.platform.infrastructure.notification.AlertBroadcaster;

/**
 * Flux temps réel des alertes pour la supervision (P2-1 / ADR-0007).
 *
 * <p>Server-Sent Events plutôt que WebSocket : le besoin est strictement descendant
 * (serveur → superviseur), et SSE reste du HTTP standard — reconnexion automatique côté
 * navigateur, compatible proxies et en-tête {@code Authorization}.
 *
 * <p>Endpoint séparé d'{@code AlertController} à dessein : le streaming a un cycle de vie et un
 * type de média propres, et {@code AlertController} porte déjà des chemins particuliers
 * ({@code @GetMapping("s")}) qu'on évite d'alourdir.
 */
@RestController
@RequestMapping("/v1/alerts")
public class AlertStreamController {

    private final AlertBroadcaster alertBroadcaster;

    public AlertStreamController(AlertBroadcaster alertBroadcaster) {
        this.alertBroadcaster = alertBroadcaster;
    }

    @Operation(
            summary = "Flux temps réel des alertes (SSE)",
            description = """
                    Ouvre un flux `text/event-stream` poussant les alertes dès leur création,
                    sans polling. Types d'événements émis :
                    `connected` (accusé d'ouverture), `alert` (charge utile : l'alerte),
                    `heartbeat` (maintien de connexion).

                    Le flux est authentifié comme le reste de `/v1/**`.
                    Exemple : `new EventSource('/v1/alerts/stream')` côté navigateur.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Flux ouvert"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(Authentication authentication) {
        // `Authentication` est null si la sécurité venait à être désactivée sur ce chemin :
        // on retombe sur un identifiant anonyme plutôt que de lever une NPE.
        String userId = authentication != null ? authentication.getName() : "anonymous";
        return alertBroadcaster.subscribe(userId);
    }
}
