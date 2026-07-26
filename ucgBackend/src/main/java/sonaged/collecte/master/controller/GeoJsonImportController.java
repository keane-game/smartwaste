package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sonaged.collecte.master.service.geojson.GeoJsonImportService;

import java.util.Map;

/**
 * Déclenchement manuel de l'import GeoJSON de référence (P1-5).
 *
 * <p>Exposé sous {@code /v1/**} : nécessite un jeton valide (contrairement aux endpoints
 * d'upload sous {@code /data/**}, laissés publics par la configuration existante).
 */
@RestController
@RequestMapping("/v1/admin/import")
@RequiredArgsConstructor
public class GeoJsonImportController {

    private final GeoJsonImportService importService;

    @Operation(summary = "Importer les GeoJSON de référence (datas/*.json) en base")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Récapitulatif de l'import par étape"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/geojson")
    public Map<String, String> importGeoJson(
            @RequestParam(value = "force", defaultValue = "false") boolean force) {
        return importService.importAll(force);
    }
}
