package sn.smartwaste.collect.identity.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sn.smartwaste.collect.identity.domain.model.UserSession;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    /** Recherche par empreinte : le jeton en clair ne circule jamais jusqu'à la base. */
    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);

    List<UserSession> findByUserIdAndRevokedAtIsNull(UUID userId);

    /** Sessions révoquées ou expirées depuis un moment — candidates au ménage. */
    List<UserSession> findByExpiresAtBefore(Instant cutoff);
}
