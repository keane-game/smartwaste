package ucg.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ucg.collecte.master.dto.CircuitBalayageDto;
import ucg.collecte.master.service.CircuitBalayageService;

import java.util.List;

@RestController
@AllArgsConstructor
public class CircuitBalayageController {
    private final CircuitBalayageService circuitBalayageService;

    @Operation(summary = "Get One CircuitBalayage by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One CircuitBalayage"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/circuit-balayage/{id}")
    public ResponseEntity<CircuitBalayageDto> getOneCircuitBalayage(@PathVariable("circuitBalayageId") Long circuitBalayageId){
        CircuitBalayageDto circuitBalayageDto = circuitBalayageService.getOneCircuitBalayage(circuitBalayageId);
        return ResponseEntity
                .ok()
                .body(circuitBalayageDto);
    }

    @Operation(summary = "Get All CircuitBalayage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All CircuitBalayages"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/circuit-balayage/all")
    public ResponseEntity<List<CircuitBalayageDto>> getAllCircuitBalayage(){
        return ResponseEntity
                .ok()
                .body(circuitBalayageService.getAllCircuitBalayage());
    }

    @Operation(summary = "Create one CircuitBalayage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one circuitBalayage"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/create/circuit-balayage")
    public ResponseEntity<CircuitBalayageDto> createOneCircuitBalayage(@RequestBody CircuitBalayageDto circuitBalayagedto){
        CircuitBalayageDto circuitBalayage = circuitBalayageService.createOneCircuitBalayage(circuitBalayagedto);
        return ResponseEntity.ok()
                .body(circuitBalayage);
    }

    @Operation(summary = "update One CircuitBalayage by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one circuitBalayage"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/update/circuit-balayage/{id}")
    public ResponseEntity<CircuitBalayageDto>  updateOneCircuitBalayage(@PathVariable("circuitBalayageId") Long circuitBalayageId, @RequestBody() CircuitBalayageDto circuitBalayageDto) {
        CircuitBalayageDto circuitBalayage = circuitBalayageService.updateOneCircuitBalayage(circuitBalayageId, circuitBalayageDto);
        return ResponseEntity.ok()
                .body(circuitBalayage);
    }

    @Operation(summary = "Delete One CircuitBalayage by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one circuitBalayage"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/circuit-balayage/{id}")
    public ResponseEntity<String> deleteOneCircuitBalayage(@PathVariable("circuitBalayageId") Long circuitBalayageId) {
        circuitBalayageService.deleteOneCircuitBalayage(circuitBalayageId);
        return ResponseEntity.ok()
                .body("Successfully delete");
    }
}
