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
import sn.smartwaste.collect.waste.application.dto.Circuit;
import sn.smartwaste.collect.waste.application.service.CircuitService;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/circuits")
public class CircuitController {

    private final CircuitService circuitService;

    @Operation(summary = "Get One Circuit by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Circuit"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{circuitId}")
    public Circuit readCircuit(@PathVariable("circuitId") UUID circuitId){
        return circuitService.readCircuit (circuitId);

    }

    @Operation(summary = "Get All Circuit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Circuits"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("s")
    public List<Circuit> readAllCircuit(){
        return circuitService.readAllCircuit ();
    }

    /**
     * Corrige une incohérence relevée par audit (2026-08-10, `docs/FRONTEND_API_MAPPING.md`) : cette
     * ressource n'avait qu'une liste plate. Même patron que Commune/Quartier/Depotoir/User/Alert.
     */
    @Operation(summary = "Read Circuit by pagination with size", description = "Read Circuits")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<Circuit> readAllCircuit(@RequestParam("page") int page, @RequestParam("size") int size){
        Pageable pageable = PageRequest.of(page, size);
        return circuitService.readAllCircuit(pageable);
    }

    @Operation(summary = "Create one Circuit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one circuit"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Circuit createCircuit(@RequestBody Circuit circuit){
        return circuitService.createCircuit (circuit);

    }

    @Operation(summary = "update One Circuit by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one circuit"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{circuitId}")
    public Circuit  updateCircuit(@PathVariable("circuitId") UUID circuitId, @RequestBody() Circuit circuit) {
        return circuitService.updateCircuit (circuitId, circuit);

    }

    @Operation(summary = "Delete One Circuit by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one circuit"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{circuitId}")
    public String deleteOneCircuit(@PathVariable("circuitId") UUID circuitId) {
        circuitService.deleteCircuit (circuitId);
        return "Successfully delete";
    }

}
