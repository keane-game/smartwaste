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

import sn.smartwaste.collect.waste.application.service.VehicleService;
import sn.smartwaste.collect.waste.application.service.VehicleService.VehicleDto;

/**
 * Flotte de collecte (`/v1/vehicles`).
 *
 * <p>Sans ces endpoints, la flotte n'existait qu'en théorie : le suivi de position écrivait dans une
 * table que rien ne pouvait remplir.
 *
 * <p>Réservé à l'encadrement : la composition de la flotte est une donnée d'exploitation.
 */
@RestController
@RequestMapping("/v1/vehicles")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @Operation(summary = "Lister la flotte")
    @GetMapping
    public List<VehicleDto> readAll() {
        return vehicleService.readAll();
    }

    @Operation(summary = "Ajouter un vehicule")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleDto create(@RequestBody VehicleDto vehicle) {
        return vehicleService.create(vehicle);
    }

    @Operation(summary = "Modifier un vehicule")
    @PutMapping("/{vehicleId}")
    public VehicleDto update(@PathVariable("vehicleId") UUID vehicleId, @RequestBody VehicleDto vehicle) {
        return vehicleService.update(vehicleId, vehicle);
    }

    @Operation(summary = "Retirer un vehicule de la circulation (sans le supprimer)")
    @DeleteMapping("/{vehicleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable("vehicleId") UUID vehicleId) {
        vehicleService.deactivate(vehicleId);
    }
}
