package sn.smartwaste.collect.waste.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.smartwaste.collect.waste.application.dto.Circuit;
import sn.smartwaste.collect.waste.application.service.CircuitService;

import java.util.List;

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
    public Circuit readCircuit(@PathVariable("circuitId") Long circuitId){
        return circuitService.readCircuit (circuitId);

    }

    @Operation(summary = "Get All Circuit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Circuits"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<Circuit> readAllCircuit(){
        return circuitService.readAllCircuit ();
    }

    @Operation(summary = "Create one Circuit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one circuit"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
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
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{circuitId}")
    public Circuit  updateCircuit(@PathVariable("circuitId") Long circuitId, @RequestBody() Circuit circuit) {
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
    public String deleteOneCircuit(@PathVariable("circuitId") Long circuitId) {
        circuitService.deleteCircuit (circuitId);
        return "Successfully delete";
    }

}
