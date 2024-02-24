package sonaged.collecte.master.repository;

import org.springframework.data.repository.CrudRepository;
import sonaged.collecte.master.model.Utilisateur;

import java.util.Optional;

public interface UtilisateurRepository extends CrudRepository<Utilisateur, Integer> {
    Optional<Utilisateur> findByEmail(String email);
}
