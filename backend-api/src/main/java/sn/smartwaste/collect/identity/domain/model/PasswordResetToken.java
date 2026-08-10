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
 * Jeton de réinitialisation de mot de passe (ADR-0021, pont avant Keycloak).
 *
 * <p><b>Table dédiée, pas une réutilisation de {@link Validation}.</b> {@code Validation} sert
 * l'activation de compte avec une durée de vie de ~972 jours (valeur historique conservée) — adaptée
 * à un e-mail qu'on peut ouvrir n'importe quand après inscription, inadaptée à un secret de
 * réinitialisation qui doit expirer vite. Mélanger les deux espaces de codes créerait un risque de
 * confusion fonctionnelle (un code d'activation ne doit jamais pouvoir réinitialiser un mot de
 * passe, et réciproquement).
 *
 * <p>Même logique que {@link UserSession} : le secret n'est <b>jamais</b> stocké en clair, seule son
 * empreinte SHA-256 l'est ({@code tokenHash}) — une base exfiltrée ne permet pas de rejouer une
 * réinitialisation.
 */
@Entity
@Table(name = "passwordresettoken")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordResetToken {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "tokenid")
    UUID tokenId;

    /** Titulaire du jeton (contexte « Identité & Accès », même contexte : simple identifiant). */
    @Column(name = "userid", nullable = false)
    UUID userId;

    /** Empreinte SHA-256 du jeton de réinitialisation — jamais le jeton lui-même. */
    @Column(name = "tokenhash", nullable = false, unique = true, length = 64)
    String tokenHash;

    @Column(name = "expiresat", nullable = false)
    Instant expiresAt;

    /** Non nul dès que le jeton a servi (avec succès ou non) — usage unique. */
    @Column(name = "usedat")
    Instant usedAt;

    /** Utilisable : ni déjà employé, ni expiré. */
    public boolean isUsable(Instant now) {
        return usedAt == null && expiresAt != null && now.isBefore(expiresAt);
    }
}
