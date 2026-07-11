package sonaged.collecte.master.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.model.Avis;
import sonaged.collecte.master.model.UserEntity;
import sonaged.collecte.master.repository.AvisRepository;

@AllArgsConstructor
@Service
public class AvisService {

    private final AvisRepository avisRepository;

    public void create(Avis avis){
       UserEntity user = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       avis.setUser(user);
        this.avisRepository.save(avis);
    }
}
