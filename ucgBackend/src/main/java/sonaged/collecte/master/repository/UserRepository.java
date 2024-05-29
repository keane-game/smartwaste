package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.AuthorityEntity;
import sonaged.collecte.master.model.UserEntity;
import sonaged.collecte.master.model.Utilisateur;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByAuthority(AuthorityEntity authority);
    Optional<UserEntity> findByUserEmail(String email);
    //User findByUserEmail(String email);
}
