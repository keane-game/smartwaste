package sn.smartwaste.collect.identity.presentation.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sn.smartwaste.collect.identity.domain.model.Permission;

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

    @Operation(summary = "Lister les permissions existantes")
    @GetMapping
    public List<Permission> readAll() {
        return List.of(Permission.values());
    }
}
