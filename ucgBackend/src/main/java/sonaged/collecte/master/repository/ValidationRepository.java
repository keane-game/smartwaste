package sonaged.collecte.master.repository;

import org.springframework.data.repository.CrudRepository;
import sonaged.collecte.master.model.Validation;

import java.util.Optional;

public interface ValidationRepository extends CrudRepository<Validation, Integer> {

    Optional<Validation> findByCode(String code);
}
