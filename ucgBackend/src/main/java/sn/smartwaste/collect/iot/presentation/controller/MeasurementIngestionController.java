package sn.smartwaste.collect.iot.presentation.controller;

import java.time.Instant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sn.smartwaste.collect.iot.application.service.MeasurementIngestionService;

/**
 * Point d'entrée des capteurs (ADR-0004 §2).
 *
 * <p>Transport REST/HTTP pour la v1 : simple, testable, et suffisant au débit actuel. Le passage à
 * un broker MQTT n'aura pas à toucher la règle métier, puisque celle-ci est déjà branchée sur un
 * événement de domaine.
 *
 * <p>Authentification par en-tête {@code X-Device-Key}, jamais par JWT : cet endpoint est donc
 * ouvert dans la chaîne de sécurité, et c'est le service qui rejette une clé inconnue en 401.
 */
@RestController
@RequestMapping("/v1/measurements")
public class MeasurementIngestionController {

    /** Nom de l'en-tête portant la clé du capteur. */
    public static final String DEVICE_KEY_HEADER = "X-Device-Key";

    private final MeasurementIngestionService ingestionService;

    public MeasurementIngestionController(MeasurementIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Operation(summary = "Transmettre une mesure de capteur",
               description = "Authentification par clé de device (en-tête X-Device-Key), pas par JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mesure enregistrée"),
            @ApiResponse(responseCode = "200", description = "Doublon ignoré (réémission)"),
            @ApiResponse(responseCode = "400", description = "Charge utile incohérente"),
            @ApiResponse(responseCode = "401", description = "Capteur non reconnu")
    })
    @PostMapping
    public ResponseEntity<Void> ingest(@RequestHeader(value = DEVICE_KEY_HEADER, required = false) String deviceKey,
                                       @RequestBody MeasurementRequest request) {
        boolean created = ingestionService.ingest(deviceKey, request.fillLevelPercent(),
                request.temperatureCelsius(), request.humidityPercent(), request.measuredAt());
        // 200 sur un doublon plutôt que 409 : pour un firmware, un 4xx signifie « réessaie », ce
        // qui provoquerait exactement la boucle qu'on cherche à éviter.
        return ResponseEntity.status(created ? HttpStatus.CREATED : HttpStatus.OK).build();
    }

    /**
     * Charge utile d'une mesure. Les trois grandeurs sont facultatives — un capteur peut n'embarquer
     * que l'ultrason ou que le DHT11 — mais au moins une doit être présente.
     */
    public record MeasurementRequest(Integer fillLevelPercent,
                                     Double temperatureCelsius,
                                     Double humidityPercent,
                                     Instant measuredAt) { }
}
