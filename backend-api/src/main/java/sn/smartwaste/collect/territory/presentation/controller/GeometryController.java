package sn.smartwaste.collect.territory.presentation.controller;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.smartwaste.collect.territory.application.dto.Geometry;
import sn.smartwaste.collect.territory.application.service.GeometryService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/geometries")
public class GeometryController {
    private final GeometryService geometryService;

    @Operation(summary = "Get One Geometry by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Geometry"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{geometryId}")
    public Geometry readGeometry(@PathVariable("geometryId") UUID geometryId){
        return geometryService.readGeometry(geometryId);

    }

    @Operation(summary = "Get All Geometries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Geometries"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("s")
    public List<Geometry> readAllGeometry(){
        return geometryService.readAllGeometry();
    }

    /**
     * Corrige une incohérence relevée par audit (2026-08-10, `docs/FRONTEND_API_MAPPING.md`) : cette
     * ressource n'avait qu'une liste plate. Même patron que Commune/Quartier/Depotoir/User/Alert.
     */
    @Operation(summary = "Read Geometry by pagination with size", description = "Read Geometries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<Geometry> readAllGeometry(@RequestParam("page") int page, @RequestParam("size") int size){
        Pageable pageable = PageRequest.of(page, size);
        return geometryService.readAllGeometry(pageable);
    }

    @Operation(summary = "Create one Geometry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one geometry"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Geometry createGeometry(@RequestBody Geometry geometry){
        return geometryService.createGeometry(geometry);

    }

    @Operation(summary = "update One Geometry by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one geometry"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{geometryId}")
    public Geometry  updateGeometry(@PathVariable("geometryId") UUID geometryId, @RequestBody() Geometry geometry) {
        return geometryService.updateGeometry(geometryId, geometry);

    }

    @Operation(summary = "Delete One Geometry by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one geometry"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{geometryId}")
    public String deleteGeometry(@PathVariable("geometryId") UUID geometryId) {
        geometryService.deleteGeometry (geometryId);
        return "Successfully delete";
    }
}
