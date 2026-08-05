package sn.smartwaste.collect.platform.domain.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

/**
 * Abonnement d'un habitant aux passages de collecte de son quartier.
 *
 * <p><b>C'est la demande n°1 de l'enquête citoyenne</b> (~29 réponses sur 34 : « êtes-vous pour un
 * système d'alerte pour sortir vos ordures ? »), et elle n'était couverte nulle part. Les deux
 * problèmes les plus cités — « les voitures ne passent pas souvent » et « on oublie de sortir les
 * ordures » — se répondent par la même chose : savoir <i>quand</i> le camion passe.
 *
 * <p>À noter : le klaxon des camions, qui joue ce rôle aujourd'hui, est déclaré gênant par une large
 * majorité des répondants. Un rappel remplace un avertisseur sonore par une notification.
 *
 * <p>{@code userId} et {@code quartierId} sont des <b>références par identifiant</b> (ADR-0012)
 * vers « Identité &amp; Accès » et le référentiel territorial : aucune FK ne traverse la frontière.
 */
@Entity
@Table(name = "collectionsubscription",
       uniqueConstraints = @UniqueConstraint(name = "uk_subscription_user_quartier",
                                             columnNames = {"userId", "quartierId"}))
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CollectionSubscription {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "subscriptionId")
    UUID subscriptionId;

    @Column(name = "userId", nullable = false)
    UUID userId;

    @Column(name = "quartierId", nullable = false)
    UUID quartierId;

    // Aucune adresse e-mail ici, volontairement. Elle est résolue au moment de l'envoi, auprès
    // du contexte « Identité & Accès » (UserDirectory), qui est le seul à la détenir PROUVÉE :
    // l'inscription y envoie un code d'activation, et le compte ne s'ouvre qu'une fois saisi.
    // La stocker ici l'aurait figée (une adresse changée n'aurait plus été suivie) et, surtout,
    // la version antérieure l'acceptait DU CLIENT — un abonné authentifié pouvait donc inscrire
    // l'adresse d'un tiers à des e-mails récurrents, sans le moindre consentement.

    /** Se désabonner conserve la trace : on désactive plutôt qu'on efface. */
    @Column(name = "active", nullable = false)
    boolean active = true;
}
