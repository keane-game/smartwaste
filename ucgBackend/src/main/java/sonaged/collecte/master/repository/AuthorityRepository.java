package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.Authority;


@Repository
public interface AuthorityRepository  extends JpaRepository<Authority, Long> {
}
