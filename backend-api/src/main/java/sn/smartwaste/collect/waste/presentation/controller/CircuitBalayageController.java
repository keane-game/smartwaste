package sn.smartwaste.collect.waste.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.smartwaste.collect.waste.application.dto.CircuitBalayage;
import sn.smartwaste.collect.waste.application.service.CircuitBalayageService;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/circuit-balayages")
public class CircuitBalayageController {
    private final CircuitBalayageService circuitBalayageService;

    @Operation(summary = "Get One CircuitBalayage by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One CircuitBalayage"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{circuitBalayageId}")
    public CircuitBalayage readCircuitBalayage(@PathVariable("circuitBalayageId") UUID circuitBalayageId){
        return  circuitBalayageService.readCircuitBalayage(circuitBalayageId);

    }

    @Operation(summary = "Get All CircuitBalayage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All CircuitBalayages"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("s")
    public List<CircuitBalayage> readAllCircuitBalayage(){
        return circuitBalayageService.readAllCircuitBalayage();
    }

    /**
     * Corrige une incohérence relevée par audit (2026-08-10, `docs/FRONTEND_API_MAPPING.md`) : cette
     * ressource n'avait qu'une liste plate exposée, alors que le service portait déjà la méthode
     * paginée. Même patron que Commune/Quartier/Depotoir/User/Alert.
     */
    @Operation(summary = "Read CircuitBalayage by pagination with size", description = "Read CircuitBalayages")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<CircuitBalayage> readAllCircuitBalayage(@RequestParam("page") int page, @RequestParam("size") int size){
        Pageable pageable = PageRequest.of(page, size);
        return circuitBalayageService.readAllCircuitBalayage(pageable);
    }

    @Operation(summary = "Create one CircuitBalayage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one circuitBalayage"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CircuitBalayage createCircuitBalayage(@RequestBody CircuitBalayage circuitBalayage){
        return circuitBalayageService.createCircuitBalayage(circuitBalayage);

    }

    @Operation(summary = "update One CircuitBalayage by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one circuitBalayage"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{circuitBalayageId}")
    public CircuitBalayage  updateCircuitBalayage(@PathVariable("circuitBalayageId") UUID circuitBalayageId, @RequestBody() CircuitBalayage circuitBalayageDto) {
        return circuitBalayageService.updateCircuitBalayage(circuitBalayageId, circuitBalayageDto);

    }

    @Operation(summary = "Delete One CircuitBalayage by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one circuitBalayage"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{circuitBalayageId}")
    public String deleteCircuitBalayage(@PathVariable("circuitBalayageId") UUID circuitBalayageId) {
        circuitBalayageService.deleteCircuitBalayage (circuitBalayageId);
        return "Successfully delete";
    }
}
