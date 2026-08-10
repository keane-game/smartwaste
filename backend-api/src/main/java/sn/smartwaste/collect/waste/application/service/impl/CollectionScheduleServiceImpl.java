package sn.smartwaste.collect.waste.application.service.impl;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.tenant.application.api.CurrentTenantProvider;
import sn.smartwaste.collect.waste.application.dto.CollectionScheduleDto;
import sn.smartwaste.collect.waste.application.service.CollectionScheduleService;
import sn.smartwaste.collect.waste.application.service.CrossContextReferenceValidator;
import sn.smartwaste.collect.waste.domain.model.CollectionSchedule;
import sn.smartwaste.collect.waste.domain.repository.CollectionScheduleRepository;

/**
 * Gestion des horaires de passage.
 *
 * <p>Sans cet écran, l'alerte citoyenne tournait à vide : le planificateur lisait une table que
 * rien ne pouvait remplir. Aucune donnée source ne porte ces horaires — les GeoJSON de circuits
 * n'ont qu'une {@code frequence} en texte libre — ils doivent donc être saisis.
 *
 * <p><b>Le quartier est validé à l'écriture.</b> C'est une référence par identifiant vers un autre
 * contexte (ADR-0012), donc sans FK SQL pour la garantir. Un horaire pointant un quartier
 * inexistant ne déclencherait jamais aucun rappel, et rien ne le signalerait : la panne serait
 * silencieuse, ce qui est le pire mode de défaillance pour ce genre de service.
 */
@Service
@Transactional
public class CollectionScheduleServiceImpl implements CollectionScheduleService {

    private final CollectionScheduleRepository repository;
    private final CrossContextReferenceValidator referenceValidator;
    private final CurrentTenantProvider currentTenantProvider;

    public CollectionScheduleServiceImpl(CollectionScheduleRepository repository,
                                         CrossContextReferenceValidator referenceValidator,
                                         CurrentTenantProvider currentTenantProvider) {
        this.repository = repository;
        this.referenceValidator = referenceValidator;
        this.currentTenantProvider = currentTenantProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionScheduleDto> readAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionScheduleDto> readByQuartier(UUID quartierId) {
        return repository.findByQuartierIdAndActiveTrue(quartierId).stream().map(this::toDto).toList();
    }

    @Override
    public CollectionScheduleDto create(CollectionScheduleDto dto) {
        validate(dto);
        var schedule = new CollectionSchedule();
        apply(dto, schedule);
        schedule.setActive(dto.active() == null || dto.active());
        // ADR-0020 : jamais depuis le DTO client.
        schedule.setOrganizationId(currentTenantProvider.currentOrganizationId()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucune collectivité rattachée au compte courant : impossible de créer un horaire")));
        return toDto(repository.save(schedule));
    }

    @Override
    public CollectionScheduleDto update(UUID scheduleId, CollectionScheduleDto dto) {
        var schedule = repository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Horaire de collecte [%s] introuvable".formatted(scheduleId)));
        validate(dto);
        apply(dto, schedule);
        if (dto.active() != null) {
            schedule.setActive(dto.active());
        }
        return toDto(repository.save(schedule));
    }

    @Override
    public void deactivate(UUID scheduleId) {
        var schedule = repository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Horaire de collecte [%s] introuvable".formatted(scheduleId)));
        schedule.setActive(false);
        repository.save(schedule);
    }

    private void validate(CollectionScheduleDto dto) {
        if (dto.quartierId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le quartier est obligatoire");
        }
        if (dto.dayOfWeek() == null || dto.passageTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le jour et l'heure de passage sont obligatoires");
        }
        // Validation applicative : aucune FK ne traverse la frontière vers le référentiel.
        referenceValidator.requireQuartierExists(dto.quartierId());
    }

    private void apply(CollectionScheduleDto dto, CollectionSchedule schedule) {
        schedule.setCircuitCollectId(dto.circuitCollectId());
        schedule.setQuartierId(dto.quartierId());
        schedule.setDayOfWeek(dto.dayOfWeek());
        schedule.setPassageTime(truncateToMinute(dto.passageTime()));
    }

    /** Les secondes n'ont aucun sens pour un passage de camion, et brouilleraient la déduplication. */
    private LocalTime truncateToMinute(LocalTime time) {
        return time.withSecond(0).withNano(0);
    }

    private CollectionScheduleDto toDto(CollectionSchedule s) {
        return new CollectionScheduleDto(s.getScheduleId(), s.getCircuitCollectId(), s.getQuartierId(),
                s.getDayOfWeek(), s.getPassageTime(), s.isActive());
    }
}
