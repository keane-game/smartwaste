package sn.smartwaste.collect.waste.presentation.controller;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import sn.smartwaste.collect.waste.application.dto.CollectionScheduleDto;
import sn.smartwaste.collect.waste.application.service.CollectionScheduleService;

/**
 * Horaires de passage des circuits (`/v1/collection-schedules`).
 *
 * <p>C'est par ici que se saisit ce que le rappel citoyen envoie. Aucune donnée source ne porte ces
 * horaires : les fichiers de circuits n'ont qu'une {@code frequence} en texte libre.
 */
@RestController
@RequestMapping("/v1/collection-schedules")
public class CollectionScheduleController {

    private final CollectionScheduleService scheduleService;

    public CollectionScheduleController(CollectionScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @Operation(summary = "Lister les horaires, ou ceux d'un quartier")
    @GetMapping
    public List<CollectionScheduleDto> read(@RequestParam(value = "quartierId", required = false) UUID quartierId) {
        return quartierId == null ? scheduleService.readAll() : scheduleService.readByQuartier(quartierId);
    }

    @Operation(summary = "Créer un horaire de passage")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CollectionScheduleDto create(@RequestBody CollectionScheduleDto schedule) {
        return scheduleService.create(schedule);
    }

    @Operation(summary = "Modifier un horaire de passage")
    @PutMapping("/{scheduleId}")
    public CollectionScheduleDto update(@PathVariable("scheduleId") UUID scheduleId,
                                        @RequestBody CollectionScheduleDto schedule) {
        return scheduleService.update(scheduleId, schedule);
    }

    @Operation(summary = "Suspendre un horaire (les rappels cessent, l'historique reste)")
    @DeleteMapping("/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable("scheduleId") UUID scheduleId) {
        scheduleService.deactivate(scheduleId);
    }
}
