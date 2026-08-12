package sn.smartwaste.collect.identity.domain.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.smartwaste.collect.identity.domain.model.AuthorityEntity;
import sn.smartwaste.collect.identity.domain.model.UserEntity;
import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;

import java.util.Optional;


/**
 * Étend {@link SoftDeleteRepository} depuis le 2026-08-10 (généralisation du soft-delete,
 * `docs/FRONTEND_API_MAPPING.md`) : {@code User} était jusque-là le seul repository de ce projet à
 * supprimer réellement ses lignes (`UserServiceImpl.deleteUser` faisait un {@code delete} définitif).
 * Ce changement suffit à rattacher automatiquement les comptes à la corbeille générique
 * ({@code DeletionController}, {@code DeletionPurgeScheduler} — les deux découvrent tout repository
 * qui étend ce type, aucun câblage supplémentaire requis).
 *
 * <p>⚠️ Non traité ici, décision distincte : {@code findByUserEmail} n'exclut pas les comptes en
 * {@code PENDING_DELETION}. Un compte supprimé logiquement continue donc (a) de bloquer une
 * ré-inscription avec la même adresse et (b) de pouvoir se connecter, {@code UserEntity} ne
 * dérivant {@code isEnabled()}/etc. que d'{@code activated}, jamais de l'état de suppression. Changer
 * ce comportement toucherait à l'authentification, hors du périmètre « généraliser le soft-delete
 * dans la corbeille » — à trancher séparément si le produit veut qu'un compte supprimé cesse de
 * pouvoir se connecter.
 */
@Repository
public interface UserRepository extends SoftDeleteRepository<UserEntity, UUID> {
    UserEntity findByAuthority(AuthorityEntity authority);
    Optional<UserEntity> findByUserEmail(String email);
    //User findByUserEmail(String email);

    /**
     * Recherche par email, prénom ou nom parmi les comptes actifs — {@code q} nul ou vide rend la
     * liste complète. Exclut les comptes en attente de suppression, comme les autres listes du
     * projet (Depotoir, Commune…).
     */
    @Query("SELECT u FROM UserEntity u WHERE u.deletionStatus = sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE "
            + "AND (:q IS NULL OR :q = '' "
            + "OR LOWER(u.userEmail) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "OR LOWER(u.userFirstname) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "OR LOWER(u.userLastname) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<UserEntity> search(@Param("q") String q, Pageable pageable);
}
