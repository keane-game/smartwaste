package sn.smartwaste.collect.waste.presentation.controller;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
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

import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.waste.domain.model.AlertThreshold;
import sn.smartwaste.collect.waste.domain.repository.AlertThresholdRepository;

/**
 * Configuration des seuils d'alerte (`/v1/alert-thresholds`).
 *
 * <p>Répond au cas d'usage administrateur du mémoire : « configurer les seuils de température,
 * d'humidité et de niveau de remplissage pour déclencher les notifications ».
 *
 * <p>Un seuil sans type est le <b>défaut</b> ; un seuil avec type le remplace pour ce type. Laisser
 * une grandeur à {@code null} signifie « ne pas la surveiller » — ce n'est pas la même chose que
 * zéro, qui alerterait en permanence.
 */
@RestController
@RequestMapping("/v1/alert-thresholds")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AlertThresholdController {

    private final AlertThresholdRepository repository;

    public AlertThresholdController(AlertThresholdRepository repository) {
        this.repository = repository;
    }

    @Operation(summary = "Lister les seuils (le seuil par defaut en premier)")
    @GetMapping
    public List<AlertThreshold> readAll() {
        return repository.findAllByOrderByTypeDepotoirIdAsc();
    }

    @Operation(summary = "Definir un seuil, global ou par type de point de collecte")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertThreshold create(@RequestBody AlertThreshold threshold) {
        threshold.setThresholdId(null);
        threshold.setActive(true);
        return repository.save(threshold);
    }

    @Operation(summary = "Modifier un seuil")
    @PutMapping("/{thresholdId}")
    public AlertThreshold update(@PathVariable("thresholdId") UUID thresholdId,
                                 @RequestBody AlertThreshold body) {
        var threshold = repository.findById(thresholdId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Seuil [%s] introuvable".formatted(thresholdId)));
        // Affectation directe, y compris a null : mettre une grandeur a null est une action
        // volontaire — « cesser de surveiller la temperature » — et non un champ oublie.
        threshold.setFillLevelPercent(body.getFillLevelPercent());
        threshold.setTemperatureCelsius(body.getTemperatureCelsius());
        threshold.setHumidityPercent(body.getHumidityPercent());
        threshold.setTypeDepotoirId(body.getTypeDepotoirId());
        return repository.save(threshold);
    }

    @Operation(summary = "Desactiver un seuil (retour au defaut pour ce type)")
    @DeleteMapping("/{thresholdId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable("thresholdId") UUID thresholdId) {
        repository.findById(thresholdId).ifPresent(t -> {
            t.setActive(false);
            repository.save(t);
        });
    }
}
