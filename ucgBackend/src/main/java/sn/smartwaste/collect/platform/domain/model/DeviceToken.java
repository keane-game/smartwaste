package sn.smartwaste.collect.platform.domain.model;

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

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

/**
 * Le jeton par lequel un appareil peut être joint (G2 du backlog).
 *
 * <p><b>Ce que cela ferme.</b> La seule diffusion était SSE — une connexion HTTP maintenue, avec
 * jeton. Cela convient à un poste de supervision, pas à un téléphone. Le rappel « sortez vos
 * ordures » et les changements d'état d'un signalement n'atteignaient donc jamais quelqu'un dont
 * l'application est fermée, c'est-à-dire presque toujours.
 *
 * <p><b>Un appareil, pas un compte.</b> La même personne peut avoir un téléphone et une tablette,
 * et chacun porte son propre jeton. C'est aussi ce qui permet de n'en révoquer qu'un.
 */
@Entity
@Table(name = "DEVICETOKEN")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
public class DeviceToken extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "tokenId")
    UUID tokenId;

    @Column(name = "userId", nullable = false)
    UUID userId;

    @Column(name = "token", nullable = false, unique = true, length = 512)
    String token;

    /** {@code ANDROID}, {@code IOS}, {@code WEB} — informatif, le transport ne s'en sert pas. */
    @Column(name = "platform", length = 20)
    String platform;

    /** Dernier envoi réussi : c'est ce qui permettra un jour de purger les appareils disparus. */
    @Column(name = "lastUsedAt")
    Instant lastUsedAt;
}
