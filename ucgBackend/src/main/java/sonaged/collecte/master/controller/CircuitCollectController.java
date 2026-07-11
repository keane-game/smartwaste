package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.CircuitCollect;
import sonaged.collecte.master.service.CircuitCollectService;

import java.util.List;
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
    public CircuitCollect readCircuitCollect(@PathVariable("circuitCollectId") Long circuitCollectId){
        return circuitCollectService.readCircuitCollect(circuitCollectId);
    }

    @Operation(summary = "Get All CircuitCollect")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All CircuitCollects"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<CircuitCollect> readAllCircuitCollect(){
        return circuitCollectService.readAllCircuitCollect();
    }

    @Operation(summary = "Create one CircuitCollect")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one circuitCollect"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
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
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{circuitCollectId}")
    public CircuitCollect  updateCircuitCollect(@PathVariable("circuitCollectId") Long circuitCollectId, @RequestBody() CircuitCollect circuitCollectDto) {
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
    public String deleteOneCircuitCollect(@PathVariable("circuitCollectId") Long circuitCollectId) {
        circuitCollectService.deleteCircuitCollect (circuitCollectId);
        return "Successfully delete";
    }

}
