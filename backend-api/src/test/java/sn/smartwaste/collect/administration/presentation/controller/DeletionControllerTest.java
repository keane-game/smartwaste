package sn.smartwaste.collect.administration.presentation.controller;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.shared.domain.repository.SoftDeleteRepository;
import sn.smartwaste.collect.shared.domain.service.SoftDeleteService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Corbeille générique (`/v1/deletions`).
 *
 * <p><b>Le bug verrouillé ici.</b> {@code restore} attendait un {@code id} de type {@code Long}
 * alors que <b>toutes</b> les entités de ce projet utilisent des identifiants {@code UUID}
 * (générateur maison v7, ADR-0012) — audit du 2026-08-10, `docs/FRONTEND_API_MAPPING.md`. Ce test ne
 * peut pas prouver l'échec de liaison Spring MVC (il appelle la méthode Java directement, pas via
 * HTTP) ; il verrouille en revanche que l'identifiant transmis au service est bien un {@link UUID}
 * exploitable par {@code findById}, pas un {@code Long} qu'aucun {@code @Id UUID} ne peut recevoir.
 */
@ExtendWith(MockitoExtension.class)
class DeletionControllerTest {

    @Mock
    private SoftDeleteService softDeleteService;
    @Mock
    private SoftDeleteRepository<?, ?> regionRepository;

    @Test
    @DisplayName("restore transmet un UUID au service, pas un Long")
    void restorePassesUuidToService() {
        var controller = new DeletionController(softDeleteService, Map.of("regionRepository", regionRepository));
        UUID id = UUID.randomUUID();

        controller.restore("region", id);

        ArgumentCaptor<Object> passedId = ArgumentCaptor.forClass(Object.class);
        verify(softDeleteService).restoreById(eq(regionRepository), passedId.capture());
        org.assertj.core.api.Assertions.assertThat(passedId.getValue()).isInstanceOf(UUID.class).isEqualTo(id);
    }
}
