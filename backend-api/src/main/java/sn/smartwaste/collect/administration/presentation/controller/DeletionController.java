package sn.smartwaste.collect.administration.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;
import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;
import sn.smartwaste.collect.shared.domain.service.SoftDeleteService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Endpoints transverses de suppression logique (soft-delete) pour <b>toutes</b> les ressources.
 *
 * <p>Réutilise {@link SoftDeleteService}. Résout la ressource par nom (clé = nom d'entité en
 * minuscules, dérivé du nom du bean repository : {@code depotoirRepository -> "depotoir"}).
 * <ul>
 *   <li>{@code GET  /v1/deletions}                       — ressources soft-deletables disponibles</li>
 *   <li>{@code GET  /v1/deletions/{resource}}            — éléments en attente de suppression (+ purge prévue)</li>
 *   <li>{@code POST /v1/deletions/{resource}/{id}/restore} — restauration avant purge</li>
 * </ul>
 * La suppression logique elle-même reste sur chaque ressource ({@code DELETE /v1/<resource>s/{id}}).
 */
@RestController
@RequestMapping("/v1/deletions")
public class DeletionController {

    private final SoftDeleteService softDeleteService;
    private final Map<String, SoftDeleteRepository<?, ?>> repositoriesByResource = new HashMap<>();

    public DeletionController(SoftDeleteService softDeleteService,
                              Map<String, SoftDeleteRepository<?, ?>> repositoriesByBeanName) {
        this.softDeleteService = softDeleteService;
        repositoriesByBeanName.forEach((beanName, repository) -> {
            String key = beanName.replaceFirst("(?i)repository$", "").toLowerCase();
            repositoriesByResource.put(key, repository);
        });
    }

    @Operation(summary = "Ressources soft-deletables disponibles")
    @GetMapping
    public Set<String> resources() {
        return new TreeSet<>(repositoriesByResource.keySet());
    }

    @Operation(summary = "Éléments d'une ressource en attente de suppression (+ date de purge)")
    @GetMapping("/{resource}")
    public java.util.List<DeletionView> pending(@PathVariable("resource") String resource) {
        var repository = resolve(resource);
        return softDeleteService.pendingDeletion(repository).stream()
                .map(entity -> new DeletionView(entity, entity.getDeletionRequestedAt(),
                        softDeleteService.purgeDueAt(entity)))
                .toList();
    }

    @Operation(summary = "Restaurer un élément en attente de suppression")
    @PostMapping("/{resource}/{id}/restore")
    public AbstractAuditingEntity<?> restore(@PathVariable("resource") String resource,
                                             @PathVariable("id") Long id) {
        return softDeleteService.restoreById(resolve(resource), id);
    }

    private SoftDeleteRepository<?, ?> resolve(String resource) {
        SoftDeleteRepository<?, ?> repository = repositoriesByResource.get(resource.toLowerCase());
        if (repository == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ressource inconnue : " + resource);
        }
        return repository;
    }

    /** Vue d'un élément en attente de suppression + date de purge prévue. */
    public record DeletionView(Object item, LocalDateTime deletionRequestedAt, LocalDateTime purgeDueAt) {
    }
}
