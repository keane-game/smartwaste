package sn.smartwaste.collect.analytics.presentation.controller;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.analytics.application.dto.PointJournal;
import sn.smartwaste.collect.analytics.application.service.PointJournalService;

/**
 * Journal d'un point de collecte (`/v1/supervision/points/{id}/journal`, G8 du backlog).
 *
 * <p>Répond à la question qu'un superviseur se pose devant un point qui déborde toutes les
 * semaines : « que s'est-il passé ici ? ». Le niveau courant, seul rendu jusqu'ici, ne la
 * renseigne pas.
 *
 * <p>L'autorisation est celle de la surface d'administration, portée par
 * {@code SecurityConfiguration} : {@code /v1/supervision/**} y est réservé à {@code ADMIN} et
 * {@code SUPER_ADMIN}.
 */
@RestController
@RequestMapping("/v1/supervision/points")
public class PointJournalController {

    /** Fenêtre retenue quand l'appelant n'en donne pas : le mois écoulé. */
    private static final Duration PERIODE_PAR_DEFAUT = Duration.ofDays(30);

    private final PointJournalService journalService;
    private final Clock clock;

    public PointJournalController(PointJournalService journalService, Clock clock) {
        this.journalService = journalService;
        this.clock = clock;
    }

    @Operation(summary = "Journal d'un point de collecte",
               description = """
                    Entrelace, par ordre chronologique, les mesures recues de ses capteurs et les
                    alertes et passages qui l'ont concerne. Sans periode, le mois ecoule.""")
    @GetMapping("/{depotoirId}/journal")
    public PointJournal journal(
            @PathVariable("depotoirId") Long depotoirId,
            @Parameter(description = "Debut de periode (ISO-8601) ; defaut : il y a 30 jours")
            @RequestParam(value = "from", required = false) Instant from,
            @Parameter(description = "Fin de periode (ISO-8601) ; defaut : maintenant")
            @RequestParam(value = "to", required = false) Instant to) {
        Instant fin = to == null ? Instant.now(clock) : to;
        Instant debut = from == null ? fin.minus(PERIODE_PAR_DEFAUT) : from;
        if (!debut.isBefore(fin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fin de periode doit etre posterieure au debut");
        }

        var journal = journalService.journalFor(depotoirId, debut, fin);
        if (journal == null) {
            // 404 et non un journal vide : « ce point n'a pas d'histoire » et « ce point n'existe
            // pas » ne se soignent pas de la meme facon.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Point de collecte inconnu : " + depotoirId);
        }
        return journal;
    }
}
