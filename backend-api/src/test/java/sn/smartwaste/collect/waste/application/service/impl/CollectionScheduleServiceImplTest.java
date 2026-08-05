package sn.smartwaste.collect.waste.application.service.impl;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.waste.application.dto.CollectionScheduleDto;
import sn.smartwaste.collect.waste.application.service.CrossContextReferenceValidator;
import sn.smartwaste.collect.waste.domain.model.CollectionSchedule;
import sn.smartwaste.collect.waste.domain.repository.CollectionScheduleRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Saisie des horaires de passage.
 *
 * <p>Ce service est ce qui rend l'alerte citoyenne opérante : sans lui, le planificateur lisait une
 * table que rien ne pouvait remplir. Les garanties testées ici protègent contre une panne
 * <b>silencieuse</b> — un horaire accepté mais jamais déclenché serait pire qu'un refus.
 */
@ExtendWith(MockitoExtension.class)
class CollectionScheduleServiceImplTest {

    private static final UUID QUARTIER = UUID.randomUUID();
    private static final UUID SCHEDULE = UUID.randomUUID();

    @Mock
    private CollectionScheduleRepository repository;
    @Mock
    private CrossContextReferenceValidator referenceValidator;

    @InjectMocks
    private CollectionScheduleServiceImpl service;

    private static CollectionScheduleDto dto(UUID quartierId, DayOfWeek day, LocalTime time) {
        return new CollectionScheduleDto(null, 7L, quartierId, day, time, null);
    }

    private CollectionSchedule captureSaved() {
        ArgumentCaptor<CollectionSchedule> saved = ArgumentCaptor.forClass(CollectionSchedule.class);
        verify(repository).save(saved.capture());
        return saved.getValue();
    }

    @Test
    @DisplayName("un horaire valide est créé actif, à la minute près")
    void validScheduleIsCreatedActive() {
        when(repository.save(any(CollectionSchedule.class))).thenAnswer(i -> i.getArgument(0));

        service.create(dto(QUARTIER, DayOfWeek.TUESDAY, LocalTime.of(7, 30, 45)));

        CollectionSchedule saved = captureSaved();
        assertThat(saved.getQuartierId()).isEqualTo(QUARTIER);
        assertThat(saved.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
        // Les secondes n'ont aucun sens pour un passage de camion et brouilleraient la
        // déduplication du planificateur, qui indexe sur (quartier, heure).
        assertThat(saved.getPassageTime()).isEqualTo(LocalTime.of(7, 30));
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    @DisplayName("un quartier inexistant est refusé, et rien n'est enregistré")
    void unknownQuartierIsRejected() {
        doThrow(new ResourceNotFoundException("Quartier introuvable"))
                .when(referenceValidator).requireQuartierExists(QUARTIER);

        assertThatThrownBy(() -> service.create(dto(QUARTIER, DayOfWeek.MONDAY, LocalTime.of(8, 0))))
                .isInstanceOf(ResourceNotFoundException.class);

        // Sans FK SQL vers le référentiel (ADR-0012), c'est la seule barrière. Un horaire pointant
        // un quartier inexistant ne déclencherait jamais de rappel, sans que rien ne le signale.
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("jour, heure ou quartier manquant : refus explicite plutôt qu'horaire inerte")
    void missingFieldsAreRejected() {
        assertThatThrownBy(() -> service.create(dto(QUARTIER, null, LocalTime.of(8, 0))))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service.create(dto(QUARTIER, DayOfWeek.MONDAY, null)))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service.create(dto(null, DayOfWeek.MONDAY, LocalTime.of(8, 0))))
                .isInstanceOf(ResponseStatusException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("suspendre désactive sans supprimer")
    void deactivateKeepsTheRow() {
        var schedule = new CollectionSchedule();
        schedule.setActive(true);
        when(repository.findById(SCHEDULE)).thenReturn(Optional.of(schedule));

        service.deactivate(SCHEDULE);

        assertThat(schedule.isActive()).isFalse();
        verify(repository).save(schedule);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("modifier un horaire inconnu lève ResourceNotFoundException")
    void updatingUnknownScheduleThrows() {
        when(repository.findById(SCHEDULE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(SCHEDULE, dto(QUARTIER, DayOfWeek.MONDAY, LocalTime.of(8, 0))))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
