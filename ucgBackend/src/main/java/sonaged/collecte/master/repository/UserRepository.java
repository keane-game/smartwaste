package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.Authority;
import sonaged.collecte.master.model.User;
import sonaged.collecte.master.model.Utilisateur;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByAuthority(Authority authority);
    Optional<User> findByUserEmail(String email);
    //User findByUserEmail(String email);
}
