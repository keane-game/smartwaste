package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
