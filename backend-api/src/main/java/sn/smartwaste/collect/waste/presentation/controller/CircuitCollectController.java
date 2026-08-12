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
import sn.smartwaste.collect.waste.application.dto.CircuitCollect;
import sn.smartwaste.collect.waste.application.service.CircuitCollectService;

import java.util.List;
import java.util.UUID;
@RestController
@AllArgsConstructor
@RequestMapping("/v1/circuit-collects")
public class CircuitCollectController {

    private final CircuitCollectService circuitCollectService;

    @Operation(summary = "Get One CircuitCollect by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One CircuitCollect"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{circuitCollectId}")
    public CircuitCollect readCircuitCollect(@PathVariable("circuitCollectId") UUID circuitCollectId){
        return circuitCollectService.readCircuitCollect(circuitCollectId);
    }

    @Operation(summary = "Get All CircuitCollect")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All CircuitCollects"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("s")
    public List<CircuitCollect> readAllCircuitCollect(){
        return circuitCollectService.readAllCircuitCollect();
    }

    /**
     * Corrige une incohérence relevée par audit (2026-08-10, `docs/FRONTEND_API_MAPPING.md`) : cette
     * ressource n'avait qu'une liste plate exposée, alors que le service portait déjà la méthode
     * paginée. Même patron que Commune/Quartier/Depotoir/User/Alert.
     */
    @Operation(summary = "Read CircuitCollect by pagination with size", description = "Read CircuitCollects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<CircuitCollect> readAllCircuitCollect(@RequestParam("page") int page, @RequestParam("size") int size){
        Pageable pageable = PageRequest.of(page, size);
        return circuitCollectService.readAllCircuitCollect(pageable);
    }

    @Operation(summary = "Create one CircuitCollect")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one circuitCollect"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CircuitCollect createCircuitCollect(@RequestBody CircuitCollect circuitCollect){
        return circuitCollectService.createCircuitCollect(circuitCollect);

    }

    @Operation(summary = "update One CircuitCollect by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one circuitCollect"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{circuitCollectId}")
    public CircuitCollect  updateCircuitCollect(@PathVariable("circuitCollectId") UUID circuitCollectId, @RequestBody() CircuitCollect circuitCollectDto) {
        return circuitCollectService.updateCircuitCollect(circuitCollectId, circuitCollectDto);

    }

    @Operation(summary = "Delete One CircuitCollect by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one circuitCollect"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{circuitCollectId}")
    public String deleteOneCircuitCollect(@PathVariable("circuitCollectId") UUID circuitCollectId) {
        circuitCollectService.deleteCircuitCollect (circuitCollectId);
        return "Successfully delete";
    }

}
