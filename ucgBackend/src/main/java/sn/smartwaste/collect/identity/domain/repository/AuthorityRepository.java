package sn.smartwaste.collect.identity.domain.repository;

import java.util.Optional;
import java.util.UUID;

import sn.smartwaste.collect.shared.domain.model.DeletionStatus;
import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.smartwaste.collect.identity.domain.model.AuthorityEntity;


@Repository
public interface AuthorityRepository  extends SoftDeleteRepository<AuthorityEntity, UUID> {

    /**
     * Recherche un rôle par son nom, en excluant les rôles supprimés logiquement.
     *
     * <p>Le filtre sur {@link DeletionStatus} est délibéré : un rôle mis à la corbeille ne doit
     * plus pouvoir être attribué. Sans lui, une inscription rattacherait silencieusement le
     * nouveau compte à un rôle en attente de purge, qui disparaîtrait au passage du planificateur.
     */
    Optional<AuthorityEntity> findByNameAndDeletionStatus(String name, DeletionStatus deletionStatus);
}
