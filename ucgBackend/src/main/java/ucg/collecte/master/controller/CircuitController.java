package ucg.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ucg.collecte.master.dto.CircuitDto;
import ucg.collecte.master.service.CircuitService;

import java.util.List;

@RestController
@AllArgsConstructor
public class CircuitController {

    private final CircuitService circuitService;

    @Operation(summary = "Get One Circuit by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Circuit"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/circuit/{id}")
    public ResponseEntity<CircuitDto> getOneCircuit(@PathVariable("circuitId") Long circuitId){
        CircuitDto circuitDto = circuitService.getOneCircuit(circuitId);
        return ResponseEntity
                .ok()
                .body(circuitDto);
    }

    @Operation(summary = "Get All Circuit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Circuits"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/circuits/all")
    public ResponseEntity<List<CircuitDto>> getAllCircuit(){
        return ResponseEntity
                .ok()
                .body(circuitService.getAllCircuit());
    }

    @Operation(summary = "Create one Circuit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one circuit"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/create/circuit")
    public ResponseEntity<CircuitDto> createOneCircuit(@RequestBody CircuitDto circuitdto){
        CircuitDto circuit = circuitService.createOneCircuit(circuitdto);
        return ResponseEntity.ok()
                .body(circuit);
    }

    @Operation(summary = "update One Circuit by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one circuit"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/update/circuit/{id}")
    public ResponseEntity<CircuitDto>  updateOneCircuit(@PathVariable("circuitId") Long circuitId, @RequestBody() CircuitDto circuitDto) {
        CircuitDto circuit = circuitService.updateOneCircuit(circuitId, circuitDto);
        return ResponseEntity.ok()
                .body(circuit);
    }

    @Operation(summary = "Delete One Circuit by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one circuit"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/circuit/{id}")
    public ResponseEntity<String> deleteOneCircuit(@PathVariable("circuitId") Long circuitId) {
        circuitService.deleteOneCircuit(circuitId);
        return ResponseEntity.ok()
                .body("Successfully delete");
    }

}
