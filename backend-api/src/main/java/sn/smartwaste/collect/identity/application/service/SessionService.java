package sn.smartwaste.collect.identity.application.service;

import java.util.UUID;

import sn.smartwaste.collect.identity.application.dto.AuthTokens;
import sn.smartwaste.collect.identity.domain.model.UserEntity;

/** Ouverture, rafraîchissement et fermeture des sessions d'authentification. */
public interface SessionService {

    /** Ouvre une session et délivre le couple de jetons. */
    AuthTokens openSession(UserEntity user);

    /**
     * Échange un jeton de rafraîchissement contre un nouveau couple, en <b>tournant</b> le jeton.
     *
     * @throws sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException si le jeton
     *         est inconnu, déjà révoqué ou expiré
     */
    AuthTokens refresh(String refreshToken);

    /** Ferme la session portée par le jeton d'accès courant. Idempotent. */
    void revoke(UUID sessionId);

    /** Ferme toutes les sessions ouvertes d'un utilisateur (changement de mot de passe, incident). */
    int revokeAllForUser(UUID userId);

    /** Vrai si la session est encore ouverte et non expirée — appelé à chaque requête. */
    boolean isActive(UUID sessionId);
}
