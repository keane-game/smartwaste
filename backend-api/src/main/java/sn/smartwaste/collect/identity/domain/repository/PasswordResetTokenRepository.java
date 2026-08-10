package sn.smartwaste.collect.identity.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sn.smartwaste.collect.identity.domain.model.PasswordResetToken;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    /** Recherche par empreinte : le jeton en clair ne circule jamais jusqu'à la base. */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** Jetons encore utilisables d'un utilisateur — à invalider quand une nouvelle demande arrive. */
    List<PasswordResetToken> findByUserIdAndUsedAtIsNull(UUID userId);
}
