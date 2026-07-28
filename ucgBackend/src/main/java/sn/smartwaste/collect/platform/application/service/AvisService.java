package sn.smartwaste.collect.platform.application.service;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.platform.domain.model.Avis;
import sn.smartwaste.collect.platform.domain.repository.AvisRepository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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

    /** Statut initial imposé par le serveur ; jamais fourni par le client. */
    private static final String STATUT_INITIAL = "EN_ATTENTE";

    public void create(Avis avis){
       // Champs contrôlés par le serveur : on neutralise ce que le client aurait pu injecter
       // dans le corps de la requête (liaison directe de l'entité JPA).
       //  - id remis à 0 -> `save()` fait toujours un INSERT et ne peut plus écraser
       //    l'avis d'un autre utilisateur (IDOR) ;
       //  - statut réinitialisé -> le client ne peut pas auto-valider son avis (mass assignment) ;
       //  - auteur imposé depuis le principal authentifié, jamais depuis le corps de la requête.
       avis.setId(0);
       avis.setStatut(STATUT_INITIAL);
       avis.setUserId(currentUserProvider.requireCurrentUserId());
        this.avisRepository.save(avis);
    }
}
