package sn.smartwaste.collect.identity.domain.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import sn.smartwaste.collect.identity.domain.model.Validation;

import java.util.Optional;

public interface ValidationRepository extends CrudRepository<Validation, UUID> {

    Optional<Validation> findByCode(String code);
}
