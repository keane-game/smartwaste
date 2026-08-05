package sn.smartwaste.collect.analytics.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.AllArgsConstructor;
import sn.smartwaste.collect.territory.application.api.DepartmentMaps;
import sn.smartwaste.collect.territory.application.api.TerritoryReadModel;
import sn.smartwaste.collect.waste.application.api.DepotoirMaps;
import sn.smartwaste.collect.waste.application.api.WasteReadModel;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/maps")
/**
 * Fond de carte de supervision (`/v1/maps/**`).
 *
 * <p>Rattaché à « Supervision &amp; Analytique » : c'est un <b>read-side</b>, au même titre que le
 * tableau de bord. Les read-models sont en revanche <b>produits</b> par les contextes propriétaires
 * — le référentiel territorial pour le contour du département, le cœur métier déchets pour les
 * points de collecte — et consommés ici via leurs interfaces publiées. Ce contrôleur assemble, il
 * ne calcule rien.
 */
public class MapsController {

    private final TerritoryReadModel territoryReadModel;
    private final WasteReadModel wasteReadModel;
    @Operation(summary = "Get One Department by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Department"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/departments")
    public DepartmentMaps getFirstDepartment(){
        return territoryReadModel.firstDepartmentForMap();
    }

    @Operation(summary = "Read Depotoir by pagination with size", description = "Read Depotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "404", description = "Resource access does not exist"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")

    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/depotoirs")
    public List<DepotoirMaps> getDepotoirMap(){
        return wasteReadModel.collectionPointsForMap();
    }

    @Operation(summary = "Vehicules de collecte en circulation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Positions recentes de la flotte"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/vehicles")
    public List<WasteReadModel.VehicleOnMap> getVehicles() {
        // Le contexte proprietaire decide de ce qui est « en circulation » : la fraicheur est une
        // regle metier, pas un parametre que l'appelant choisit.
        return wasteReadModel.vehiclesOnMap();
    }
}
