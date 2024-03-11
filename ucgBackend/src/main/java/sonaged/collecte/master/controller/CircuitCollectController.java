package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.CircuitCollectDto;
import sonaged.collecte.master.service.CircuitCollectService;

import java.util.List;
@RestController
@AllArgsConstructor
@RequestMapping("api")
public class CircuitCollectController {

    private final CircuitCollectService circuitCollectService;

    @Operation(summary = "Get One CircuitCollect by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One CircuitCollect"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/circuit-collect/{circuitCollectId}")
    public ResponseEntity<CircuitCollectDto> getOneCircuitCollect(@PathVariable("circuitCollectId") Long circuitCollectId){
        CircuitCollectDto circuitCollectDto = circuitCollectService.getOneCircuitCollect(circuitCollectId);
        return ResponseEntity
                .ok()
                .body(circuitCollectDto);
    }

    @Operation(summary = "Get All CircuitCollect")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All CircuitCollects"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/circuit-collect/all")
    public ResponseEntity<List<CircuitCollectDto>> getAllCircuitCollect(){
        return ResponseEntity
                .ok()
                .body(circuitCollectService.getAllCircuitCollect());
    }

    @Operation(summary = "Create one CircuitCollect")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one circuitCollect"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/create/circuit-collect")
    public ResponseEntity<CircuitCollectDto> createOneCircuitCollect(@RequestBody CircuitCollectDto circuitCollectdto){
        CircuitCollectDto circuitCollect = circuitCollectService.createOneCircuitCollect(circuitCollectdto);
        return ResponseEntity.ok()
                .body(circuitCollect);
    }

    @Operation(summary = "update One CircuitCollect by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one circuitCollect"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/update/circuit-collect/{circuitCollectId}")
    public ResponseEntity<CircuitCollectDto>  updateOneCircuitCollect(@PathVariable("circuitCollectId") Long circuitCollectId, @RequestBody() CircuitCollectDto circuitCollectDto) {
        CircuitCollectDto circuitCollect = circuitCollectService.updateOneCircuitCollect(circuitCollectId, circuitCollectDto);
        return ResponseEntity.ok()
                .body(circuitCollect);
    }

    @Operation(summary = "Delete One CircuitCollect by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one circuitCollect"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/circuit-collect/{circuitCollectId}")
    public ResponseEntity<String> deleteOneCircuitCollect(@PathVariable("circuitCollectId") Long circuitCollectId) {
        circuitCollectService.deleteOneCircuitCollect(circuitCollectId);
        return ResponseEntity.ok()
                .body("Successfully delete");
    }

}
