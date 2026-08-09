package sn.smartwaste.collect.tenant.presentation.controller;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.identity.application.api.UserDirectory;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.shared.domain.model.DeletionStatus;
import sn.smartwaste.collect.tenant.application.api.CurrentTenantProvider;
import sn.smartwaste.collect.tenant.domain.model.Organization;
import sn.smartwaste.collect.tenant.domain.model.OrganizationMembership;
import sn.smartwaste.collect.tenant.domain.model.OrganizationStatus;
import sn.smartwaste.collect.tenant.domain.repository.OrganizationMembershipRepository;
import sn.smartwaste.collect.tenant.domain.repository.OrganizationRepository;

/**
 * Gestion des collectivités clientes (`/v1/organizations`) — contrat figé par
 * {@code docs/adr/0020-cloisonnement-multi-tenant-et-api-organisation.md} §5 (à suivre à la
 * lettre : une session concurrente a accepté cet ADR le même jour, ne pas diverger sans y
 * retoucher).
 *
 * <p><b>Onboarder une collectivité est un geste plateforme</b>, réservé {@code MANAGE_ORGANIZATIONS}
 * (semée uniquement sur SUPER_ADMIN, changelog {@code 2.25.0}) — création, modification,
 * rattachement/détachement de membres. <b>Consulter</b> une collectivité (détail + membres) est en
 * revanche ouvert à l'ADMIN de <i>cette</i> collectivité, pas seulement à SUPER_ADMIN — contrôle
 * applicatif, pas juste l'URL : voir {@link #requireSuperAdminOrOwnOrganization}.
 *
 * <p><b>Ce que cet écran ne fait PAS</b> : aucun agrégat métier ne porte encore de
 * {@code organizationId} (ADR-0020 est le plan, pas encore le cloisonnement effectif — vague
 * suivante). Détacher un membre ici n'a donc, à ce stade, aucun effet sur son accès aux données.
 */
@RestController
@RequestMapping("/v1/organizations")
public class OrganizationController {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final UserDirectory userDirectory;
    private final CurrentUserProvider currentUserProvider;
    private final CurrentTenantProvider currentTenantProvider;

    public OrganizationController(OrganizationRepository organizationRepository,
                                  OrganizationMembershipRepository membershipRepository,
                                  UserDirectory userDirectory,
                                  CurrentUserProvider currentUserProvider,
                                  CurrentTenantProvider currentTenantProvider) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.userDirectory = userDirectory;
        this.currentUserProvider = currentUserProvider;
        this.currentTenantProvider = currentTenantProvider;
    }

    @Operation(summary = "Lister les collectivités (actives et suspendues)")
    @PreAuthorize("hasAuthority('MANAGE_ORGANIZATIONS')")
    @GetMapping
    public List<Organization> readAll() {
        return organizationRepository.findByDeletionStatus(DeletionStatus.ACTIVE);
    }

    @Operation(summary = "Consulter une collectivité",
               description = "SUPER_ADMIN, ou ADMIN de cette collectivité.")
    @GetMapping("/{organizationId}")
    public Organization read(@PathVariable("organizationId") UUID organizationId) {
        requireSuperAdminOrOwnOrganization(organizationId);
        return findOrThrow(organizationId);
    }

    @Operation(summary = "Créer une collectivité",
               description = "Le statut est toujours ACTIVE à la création, quoi que le client envoie.")
    @PreAuthorize("hasAuthority('MANAGE_ORGANIZATIONS')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Organization create(@RequestBody Organization body) {
        if (body.getCode() != null && organizationRepository.findByCode(body.getCode()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le code [%s] est déjà utilisé par une autre collectivité".formatted(body.getCode()));
        }
        // Neutralise ce qu'un appelant pourrait tenter de forcer : un id existant ferait faire un
        // UPDATE (merge) au lieu d'un INSERT (même faille que celle fermée sur Avis/User — voir
        // AuthServiceImpl.register), et le statut doit toujours démarrer à ACTIVE.
        body.setOrganizationId(null);
        body.setStatus(OrganizationStatus.ACTIVE);
        return organizationRepository.save(body);
    }

    @Operation(summary = "Modifier nom et/ou statut d'une collectivité",
               description = "Le code est immuable après création. Pas d'endpoint de suppression : "
                       + "une collectivité se suspend (status=SUSPENDED), elle ne se supprime pas.")
    @PreAuthorize("hasAuthority('MANAGE_ORGANIZATIONS')")
    @PutMapping("/{organizationId}")
    public Organization update(@PathVariable("organizationId") UUID organizationId,
                               @RequestBody Organization body) {
        var organization = findOrThrow(organizationId);
        if (body.getName() != null) {
            organization.setName(body.getName());
        }
        if (body.getStatus() != null) {
            organization.setStatus(body.getStatus());
        }
        return organizationRepository.save(organization);
    }

    @Operation(summary = "Lister les membres rattachés à une collectivité",
               description = "SUPER_ADMIN, ou ADMIN de cette collectivité.")
    @GetMapping("/{organizationId}/members")
    public List<OrganizationMembership> members(@PathVariable("organizationId") UUID organizationId) {
        requireSuperAdminOrOwnOrganization(organizationId);
        findOrThrow(organizationId);
        return membershipRepository.findByOrganizationId(organizationId);
    }

    @Operation(summary = "Rattacher un utilisateur existant à une collectivité",
               description = "409 si l'utilisateur a déjà un rattachement (une seule collectivité par personne aujourd'hui).")
    @PreAuthorize("hasAuthority('MANAGE_ORGANIZATIONS')")
    @PostMapping("/{organizationId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationMembership attach(@PathVariable("organizationId") UUID organizationId,
                                         @RequestBody MembershipRequest body) {
        findOrThrow(organizationId);
        if (body.userId() == null || userDirectory.emailOf(body.userId()).isEmpty()) {
            throw new ResourceNotFoundException(
                    "Utilisateur référencé [%s] introuvable".formatted(body.userId()));
        }
        if (membershipRepository.findByUserId(body.userId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cet utilisateur est déjà rattaché à une collectivité");
        }
        var membership = new OrganizationMembership();
        membership.setUserId(body.userId());
        membership.setOrganizationId(organizationId);
        return membershipRepository.save(membership);
    }

    @Operation(summary = "Détacher un utilisateur d'une collectivité")
    @PreAuthorize("hasAuthority('MANAGE_ORGANIZATIONS')")
    @DeleteMapping("/{organizationId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void detach(@PathVariable("organizationId") UUID organizationId,
                       @PathVariable("userId") UUID userId) {
        var membership = membershipRepository.findByUserId(userId)
                .filter(m -> m.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur [%s] non rattaché à cette collectivité".formatted(userId)));
        membershipRepository.delete(membership);
    }

    /**
     * SUPER_ADMIN passe toujours ; un ADMIN ne passe que pour SA propre collectivité — comparaison
     * de {@code organizationId}, pas seulement un contrôle d'URL (ADR-0020 §5).
     */
    private void requireSuperAdminOrOwnOrganization(UUID organizationId) {
        if (currentUserProvider.currentUserHasAnyRole("SUPER_ADMIN")) {
            return;
        }
        boolean ownOrganization = currentUserProvider.currentUserHasAnyRole("ADMIN")
                && currentTenantProvider.currentOrganizationId()
                        .map(organizationId::equals)
                        .orElse(false);
        if (!ownOrganization) {
            throw new AccessDeniedException("Hors de votre collectivité");
        }
    }

    private Organization findOrThrow(UUID organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Collectivité [%s] introuvable".formatted(organizationId)));
    }

    /** Corps de rattachement — l'utilisateur ciblé, référencé par identifiant (ADR-0012). */
    public record MembershipRequest(UUID userId) { }
}
