package sn.smartwaste.collect.platform.presentation.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.platform.application.service.AwarenessService;

/**
 * Messages de sensibilisation (`/v1/awareness`, G3 du backlog).
 *
 * <p><b>Rédiger et lire ne sont pas la même chose.</b> La rédaction est réservée à
 * l'administration — une fonction de diffusion de masse ouverte à tout compte serait une fonction
 * de nuisance. La lecture est ouverte à chacun, sur ce qu'il a lui-même reçu.
 */
@RestController
@RequestMapping("/v1/awareness")
public class AwarenessController {

    private final AwarenessService awarenessService;
    private final CurrentUserProvider currentUserProvider;

    public AwarenessController(AwarenessService awarenessService,
                               CurrentUserProvider currentUserProvider) {
        this.awarenessService = awarenessService;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "Programmer un message de sensibilisation",
               description = """
                    Sans quartier, le message vise tous les abonnes. Sans date, il part au prochain
                    passage du planificateur. Un second message vers le meme territoire dans la
                    fenetre de frequence est refuse (409).""")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public MessageCree schedule(@RequestBody AwarenessRequest body) {
        if (body == null || body.title() == null || body.title().isBlank()
                || body.body() == null || body.body().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le titre et le corps du message sont obligatoires");
        }
        var message = awarenessService.schedule(
                body.title(), body.body(), body.quartierId(), body.scheduledAt());
        return new MessageCree(message.getMessageId(), message.getScheduledAt());
    }

    @Operation(summary = "Les messages que j'ai recus",
               description = "Ceux adresses a mes quartiers d'abonnement, et ceux adresses a tous.")
    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public List<MessageRecu> mine() {
        return awarenessService.receivedBy(currentUserProvider.requireCurrentUserId()).stream()
                .map(m -> new MessageRecu(m.getTitle(), m.getBody(), m.getSentAt()))
                .toList();
    }

    /**
     * @param quartierId  {@code null} vise tous les abonnés
     * @param scheduledAt {@code null} part au prochain passage du planificateur
     */
    public record AwarenessRequest(String title, String body,
                                   @Parameter(description = "Quartier vise ; absent = tous")
                                   UUID quartierId,
                                   Instant scheduledAt) { }

    public record MessageCree(UUID messageId, Instant scheduledAt) { }

    /** Ce qu'un citoyen voit : le contenu et la date, pas le ciblage ni l'auteur. */
    public record MessageRecu(String title, String body, Instant sentAt) { }
}
