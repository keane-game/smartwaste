package ucg.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucg.collecte.master.model.Authority;


@Repository
public interface AuthorityRepository  extends JpaRepository<Authority, Long> {
}
