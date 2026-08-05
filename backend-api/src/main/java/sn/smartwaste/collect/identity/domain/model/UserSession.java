package sn.smartwaste.collect.identity.domain.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

/**
 * Session d'authentification — l'état serveur qui manquait.
 *
 * <p><b>Le problème qu'elle résout.</b> Jusqu'ici l'authentification était purement « stateless » :
 * un JWT signé, valable <b>10 jours</b>, et rien côté serveur. Conséquences concrètes :
 * la déconnexion n'existait pas (le client se contentait de vider son {@code localStorage}, le
 * jeton restant parfaitement valide), un jeton copié ne pouvait pas être révoqué, et un
 * changement de mot de passe ne fermait aucune session existante.
 *
 * <p>Une session donne un point de révocation. Le jeton d'accès porte son identifiant
 * ({@code sid}) ; le filtre vérifie à chaque requête que la session est toujours ouverte.
 *
 * <p><b>Le jeton de rafraîchissement n'est jamais stocké en clair</b> — seulement son empreinte
 * SHA-256. Une base exfiltrée ne permet donc pas de rejouer les sessions : c'est la même raison
 * qui interdit de stocker un mot de passe en clair. Il est par ailleurs <b>tourné</b> à chaque
 * rafraîchissement, de sorte qu'un jeton volé cesse de fonctionner dès que le titulaire légitime
 * s'en sert.
 */
@Entity
@Table(name = "usersession")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSession {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "sessionId")
    UUID sessionId;

    /** Titulaire de la session (contexte « Identité & Accès », même contexte : simple identifiant). */
    @Column(name = "userId", nullable = false)
    UUID userId;

    /** Empreinte SHA-256 du jeton de rafraîchissement — jamais le jeton lui-même. */
    @Column(name = "refreshTokenHash", nullable = false, unique = true, length = 64)
    String refreshTokenHash;

    @Column(name = "issuedAt", nullable = false)
    Instant issuedAt;

    /** Fin de validité du jeton de rafraîchissement. Au-delà, il faut se reconnecter. */
    @Column(name = "expiresAt", nullable = false)
    Instant expiresAt;

    /** Non nul dès que la session a été fermée (déconnexion, révocation administrative). */
    @Column(name = "revokedAt")
    Instant revokedAt;

    @Column(name = "lastUsedAt")
    Instant lastUsedAt;

    /** Ouverte, non expirée : la seule combinaison qui autorise une requête. */
    public boolean isActive(Instant now) {
        return revokedAt == null && expiresAt != null && now.isBefore(expiresAt);
    }

    public void revoke(Instant when) {
        if (this.revokedAt == null) {
            this.revokedAt = when;
        }
    }
}
