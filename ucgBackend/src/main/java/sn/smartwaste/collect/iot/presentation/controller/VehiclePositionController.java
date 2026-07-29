package sn.smartwaste.collect.iot.presentation.controller;

import java.time.Instant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import sn.smartwaste.collect.iot.application.service.VehiclePositionIngestionService;

/**
 * Point d'entrée des traceurs embarqués (`POST /v1/vehicle-positions`).
 *
 * <p>Authentification par en-tête {@code X-Device-Key}, comme les capteurs : un véhicule n'a pas
 * de session utilisateur.
 */
@RestController
@RequestMapping("/v1/vehicle-positions")
public class VehiclePositionController {

    public static final String DEVICE_KEY_HEADER = "X-Device-Key";

    private final VehiclePositionIngestionService ingestionService;

    public VehiclePositionController(VehiclePositionIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Operation(summary = "Transmettre la position d'un vehicule",
               description = "Authentification par cle de device (X-Device-Key), pas par JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Position acceptee"),
            @ApiResponse(responseCode = "400", description = "Coordonnees ou horodatage invalides"),
            @ApiResponse(responseCode = "401", description = "Traceur non reconnu")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void ingest(@RequestHeader(value = DEVICE_KEY_HEADER, required = false) String deviceKey,
                       @RequestBody PositionRequest request) {
        ingestionService.ingest(deviceKey, request.latitude(), request.longitude(), request.recordedAt());
    }

    /** @param recordedAt horodatage de la position ; l'heure de reception est prise si absent */
    public record PositionRequest(double latitude, double longitude, Instant recordedAt) { }
}
