package sn.smartwaste.collect.tenant.domain.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

/**
 * Rattachement d'un utilisateur à une collectivité.
 *
 * <p><b>Pourquoi ici et non sur {@code UserEntity}.</b> Poser un {@code organizationId} sur
 * l'utilisateur aurait été plus court, mais aurait placé une donnée de cloisonnement dans le
 * contexte « Identité &amp; Accès », qui répond à une autre question — <i>qui es-tu</i>, et non
 * <i>pour le compte de quelle collectivité</i>. Le rattachement appartient au tenant, comme
 * l'annonce déjà le {@code package-info} du module. Conséquence pratique : l'identité n'a pas eu
 * à changer, et le passage à un utilisateur intervenant sur plusieurs collectivités ne demandera
 * qu'à lever la contrainte d'unicité ci-dessous.
 *
 * <p>{@code userId} est une <b>référence par identifiant</b> (ADR-0012) : aucune FK ne traverse la
 * frontière vers l'identité.
 */
@Entity
@Table(name = "organizationmembership",
       uniqueConstraints = @UniqueConstraint(name = "uk_membership_user", columnNames = "userId"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationMembership extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "membershipId")
    UUID membershipId;

    /** Utilisateur rattaché — contexte « Identité & Accès », référencé par identifiant. */
    @Column(name = "userId", nullable = false)
    UUID userId;

    @Column(name = "organizationId", nullable = false)
    UUID organizationId;
}
