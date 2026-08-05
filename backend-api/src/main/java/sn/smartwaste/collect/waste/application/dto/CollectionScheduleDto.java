package sn.smartwaste.collect.waste.application.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Horaire de passage, tel qu'exposé par l'API.
 *
 * @param scheduleId       nul à la création
 * @param circuitCollectId circuit effectuant le passage, facultatif
 * @param quartierId       quartier desservi — obligatoire, c'est la maille d'abonnement
 * @param dayOfWeek        jour de passage
 * @param passageTime      heure de passage prévue
 * @param active           un horaire suspendu cesse de déclencher des rappels sans être supprimé
 */
public record CollectionScheduleDto(UUID scheduleId,
                                    Long circuitCollectId,
                                    UUID quartierId,
                                    DayOfWeek dayOfWeek,
                                    LocalTime passageTime,
                                    Boolean active) { }
