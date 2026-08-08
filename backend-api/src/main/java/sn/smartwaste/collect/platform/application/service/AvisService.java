package sn.smartwaste.collect.platform.application.service;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.platform.domain.model.Avis;
import sn.smartwaste.collect.platform.domain.repository.AvisRepository;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.platform.domain.model.AvisStatus;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;

@AllArgsConstructor
@Service
public class AvisService {

    private final AvisRepository avisRepository;

    /**
     * Port publié par « Identité &amp; Accès ». Remplace la lecture directe du
     * {@code SecurityContextHolder} suivie d'un cast vers {@code UserEntity} : ce contexte n'a
     * plus à connaître le modèle d'identité (ADR-0013 §3).
     */
    private final CurrentUserProvider currentUserProvider;

    /** Signalements encore ouverts — ceux qui méritent d'apparaître sur la carte. */
    private static final EnumSet<AvisStatus> OUVERTS = EnumSet.of(AvisStatus.SIGNALE, AvisStatus.EN_COURS);

    public void create(Avis avis){
       // Champs contrôlés par le serveur : on neutralise ce que le client aurait pu injecter
       // dans le corps de la requête (liaison directe de l'entité JPA).
       //  - id remis à null -> `save()` fait toujours un INSERT et ne peut plus écraser
       //    l'avis d'un autre utilisateur (IDOR) ;
       //  - statut réinitialisé -> le client ne peut pas auto-valider son avis (mass assignment) ;
       //  - auteur imposé depuis le principal authentifié, jamais depuis le corps de la requête.
       avis.setId(null);
       avis.setStatut(AvisStatus.SIGNALE);
       // Le traitement ne se déclare pas à la création : un habitant ne peut pas déposer un
       // signalement déjà clos, ni s'attribuer la clôture d'un autre.
       avis.setProcessedAt(null);
       avis.setProcessedByUserId(null);
       avis.setSubmittedAt(Instant.now());
       avis.setUserId(currentUserProvider.requireCurrentUserId());
        this.avisRepository.save(avis);
    }

    /** Signalements dans un état donné, pour la file de traitement des superviseurs. */
    @Transactional(readOnly = true)
    public List<Avis> byStatus(AvisStatus statut) {
        return avisRepository.findByStatutOrderByIdDesc(statut);
    }

    /** Signalements de l'utilisateur authentifié — un habitant suit les siens, pas ceux des autres. */
    @Transactional(readOnly = true)
    public List<Avis> mine() {
        return avisRepository.findByUserIdOrderByIdDesc(currentUserProvider.requireCurrentUserId());
    }

    /** Signalements encore ouverts et localisés, pour la carte de supervision. */
    @Transactional(readOnly = true)
    public List<Avis> openWithLocation() {
        return avisRepository.findByStatutInAndLatitudeIsNotNull(OUVERTS);
    }

    /**
     * Fait avancer un signalement dans son cycle de vie.
     *
     * <p>Les transitions sont contrôlées : sans cela, un signalement clos pourrait être rouvert par
     * un simple appel, ou passer directement de « signalé » à « traité » sans qu'aucune prise en
     * charge n'ait eu lieu — ce qui viderait la file de son sens.
     */
    @Transactional
    public Avis changeStatus(UUID avisId, AvisStatus target) {
        var avis = avisRepository.findById(avisId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Signalement [%s] introuvable".formatted(avisId)));

        var current = avis.getStatut() == null ? AvisStatus.SIGNALE : avis.getStatut();
        if (current == target) {
            return avis; // idempotent : reclasser dans le même état n'est pas une erreur
        }
        if (!current.canTransitionTo(target)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Transition %s -> %s interdite".formatted(current, target));
        }

        avis.setStatut(target);
        if (target.isTerminal()) {
            // On trace QUI a clos et QUAND : sans cela, une file traitée ne dit rien de ce qui
            // s'est réellement passé sur le terrain.
            avis.setProcessedAt(Instant.now());
            avis.setProcessedByUserId(currentUserProvider.requireCurrentUserId());
        }
        return avisRepository.save(avis);
    }
}
