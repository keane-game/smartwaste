package sonaged.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sonaged.collecte.master.model.TypeDepotoirEntity;

@Repository
public interface TypeDepotoirRepository extends SoftDeleteRepository<TypeDepotoirEntity, Long> {

    TypeDepotoirEntity findByNameIgnoreCase(String name);
}
