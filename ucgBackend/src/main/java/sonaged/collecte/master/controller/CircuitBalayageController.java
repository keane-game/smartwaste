package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.CircuitBalayage;
import sonaged.collecte.master.service.CircuitBalayageService;

import java.util.List;

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
    @GetMapping("/circuit-balayage/{circuitBalayageId}")
    public CircuitBalayage readCircuitBalayage(@PathVariable("circuitBalayageId") Long circuitBalayageId){
        return  circuitBalayageService.readCircuitBalayage(circuitBalayageId);

    }

    @Operation(summary = "Get All CircuitBalayage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All CircuitBalayages"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<CircuitBalayage> readAllCircuitBalayage(){
        return circuitBalayageService.readAllCircuitBalayage();
    }

    @Operation(summary = "Create one CircuitBalayage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one circuitBalayage"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
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
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{circuitBalayageId}")
    public CircuitBalayage  updateCircuitBalayage(@PathVariable("circuitBalayageId") Long circuitBalayageId, @RequestBody() CircuitBalayage circuitBalayageDto) {
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
    public String deleteCircuitBalayage(@PathVariable("circuitBalayageId") Long circuitBalayageId) {
        circuitBalayageService.deleteCircuitBalayage (circuitBalayageId);
        return "Successfully delete";
    }
}
