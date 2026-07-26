package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.AuthorityEntity;


@Repository
public interface AuthorityRepository  extends SoftDeleteRepository<AuthorityEntity, Long> {
}
