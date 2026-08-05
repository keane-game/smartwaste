package sn.smartwaste.collect.waste.application.service;

import java.util.List;
import java.util.UUID;

import sn.smartwaste.collect.waste.application.dto.CollectionScheduleDto;

/** Gestion des horaires de passage des circuits de collecte. */
public interface CollectionScheduleService {

    List<CollectionScheduleDto> readAll();

    List<CollectionScheduleDto> readByQuartier(UUID quartierId);

    CollectionScheduleDto create(CollectionScheduleDto schedule);

    CollectionScheduleDto update(UUID scheduleId, CollectionScheduleDto schedule);

    /** Suspend l'horaire sans le supprimer : les rappels cessent, l'historique reste. */
    void deactivate(UUID scheduleId);
}
