package sn.smartwaste.collect.analytics.application.service;

import java.time.Instant;
import java.util.UUID;

import sn.smartwaste.collect.analytics.application.dto.PointJournal;

/**
 * Journal d'un point de collecte (G8 du backlog).
 *
 * <p>Répond à la question qu'un superviseur se pose devant un point qui déborde toutes les
 * semaines : « que s'est-il passé ici ? ». Le niveau courant ne la renseigne pas.
 */
public interface PointJournalService {

    /** @return {@code null} si le point n'existe pas — un journal vide se lirait « pas d'histoire ». */
    PointJournal journalFor(UUID depotoirId, Instant from, Instant to);
}
