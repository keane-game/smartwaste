package sn.smartwaste.collect.tenant.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;
import sn.smartwaste.collect.tenant.domain.model.Organization;

@Repository
public interface OrganizationRepository extends SoftDeleteRepository<Organization, UUID> {

    Optional<Organization> findByCode(String code);
}
