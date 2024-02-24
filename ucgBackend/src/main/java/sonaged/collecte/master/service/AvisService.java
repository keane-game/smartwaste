package sonaged.collecte.master.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.model.Avis;
import sonaged.collecte.master.model.Utilisateur;
import sonaged.collecte.master.repository.AvisRepository;

@AllArgsConstructor
@Service
public class AvisService {

    private final AvisRepository avisRepository;

    public void creer(Avis avis){
       Utilisateur utilisateur = (Utilisateur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       avis.setUtilisateur(utilisateur);
        this.avisRepository.save(avis);
    }
}
