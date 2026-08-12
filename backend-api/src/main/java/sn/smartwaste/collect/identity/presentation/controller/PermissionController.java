package sn.smartwaste.collect.identity.presentation.controller;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.identity.domain.model.Permission;
import sn.smartwaste.collect.identity.domain.repository.UserRepository;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;

/**
 * Catalogue des permissions (ADR-0021, pont avant Keycloak).
 *
 * <p>Lecture seule, aucune donnée sensible : sert uniquement à construire un écran d'édition de
 * rôle côté frontend sans dupliquer l'énumération {@link Permission} côté client. Même garde que
 * {@code AuthorityController}, dont ce catalogue est le complément naturel.
 */
@RestController
@RequestMapping("/v1/permissions")
@PreAuthorize("hasAuthority('MANAGE_ROLE')")
public class PermissionController {

    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public PermissionController(UserRepository userRepository, CurrentUserProvider currentUserProvider) {
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "Lister les permissions existantes")
    @GetMapping
    public List<Permission> readAll() {
        return List.of(Permission.values());
    }

    /**
     * Permissions du compte authentifié — pas seulement son rôle.
     *
     * <p><b>Le manque que cela ferme.</b> Le JWT ne porte que le premier rôle/authority du compte
     * (voir {@code JwtService}, claim {@code role}, explicitement documenté comme informatif) —
     * jamais la liste de permissions. Un frontend qui voudrait griser un bouton selon
     * {@code DECLARE_COLLECTION} ou {@code MANAGE_DEVICES} n'avait donc aucune source fiable sans
     * dupliquer côté client la table {@code authorityPermission}.
     *
     * <p><b>Ouvert à tout authentifié</b>, contrairement au reste de ce controller
     * (`hasAuthority('MANAGE_ROLE')`) : consulter ses PROPRES permissions n'a rien à voir avec le
     * droit d'administrer les rôles d'autrui. L'annotation de méthode l'emporte sur celle de classe.
     * Exception symétrique posée dans {@code SecurityConfiguration}, avant la règle générale qui
     * réserve {@code /v1/permissions/**} à l'administration — sans elle, la chaîne de filtres
     * refuserait la requête avant même d'atteindre cette méthode (même piège déjà rencontré pour
     * l'agent de collecte et le technicien IoT).
     */
    @Operation(summary = "Lister les permissions du compte authentifié")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mine")
    @Transactional(readOnly = true)
    public Collection<Permission> mine() {
        UUID userId = currentUserProvider.requireCurrentUserId();
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur inconnu"));
        var permissions = user.getAuthority().getPermissions();
        return permissions == null ? List.of() : permissions;
    }
}
