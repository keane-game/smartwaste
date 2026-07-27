package sn.smartwaste.collect.platform.application.service;

import sn.smartwaste.collect.platform.domain.model.Avis;
import sn.smartwaste.collect.platform.domain.repository.AvisRepository;

import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.model.UserEntity;

@AllArgsConstructor
@Service
public class AvisService {

    private final AvisRepository avisRepository;

    /** Statut initial imposé par le serveur ; jamais fourni par le client. */
    private static final String STATUT_INITIAL = "EN_ATTENTE";

    public void create(Avis avis){
       UserEntity user = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       // Champs contrôlés par le serveur : on neutralise ce que le client aurait pu injecter
       // dans le corps de la requête (liaison directe de l'entité JPA).
       //  - id remis à 0 -> `save()` fait toujours un INSERT et ne peut plus écraser
       //    l'avis d'un autre utilisateur (IDOR) ;
       //  - statut réinitialisé -> le client ne peut pas auto-valider son avis (mass assignment).
       avis.setId(0);
       avis.setStatut(STATUT_INITIAL);
       avis.setUser(user);
        this.avisRepository.save(avis);
    }
}
