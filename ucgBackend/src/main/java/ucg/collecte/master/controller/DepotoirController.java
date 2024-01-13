package ucg.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ucg.collecte.master.dto.DepotDto;
import ucg.collecte.master.service.DepotService;

import java.util.List;

@RestController
@AllArgsConstructor
public class DepotController {
    private final DepotService depotService;

    @Operation(summary = "Get One Depot by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Depot"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/depot/{id}")
    public ResponseEntity<DepotDto> getOneDepot(@PathVariable("depotId") Long depotid){
        DepotDto depotDto = depotService.getOneDepot(depotid);
        return ResponseEntity
                .ok()
                .body(depotDto);
    }

    @Operation(summary = "Get All Depot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Depots"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/depots/all")
    public ResponseEntity<List<DepotDto>> getAllDepot(){
        return ResponseEntity
                .ok()
                .body(depotService.getAllDepot());
    }

    @Operation(summary = "Create one Depot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one depot"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/create/depot")
    public ResponseEntity<DepotDto> createOneDepot(@RequestBody DepotDto depotDto){
        DepotDto depot = depotService.createOneDepot(depotDto);
        return ResponseEntity.ok()
                .body(depot);
    }

    @Operation(summary = "update One Depot by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one depot"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/update/depot/{id}")
    public ResponseEntity<DepotDto>  updateOneDepot(@PathVariable("depotId") Long depotId, @RequestBody() DepotDto depotDto) {
        DepotDto depot = depotService.updateOneDepot(depotId, depotDto);
        return ResponseEntity.ok()
                .body(depot);
    }

    @Operation(summary = "Delete One Depot by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one depot"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/depot/{id}")
    public ResponseEntity<String> deleteOneDepot(@PathVariable("depotId") Long depotId) {
        depotService.deleteOneDepot(depotId);
        return ResponseEntity.ok()
                .body("Successfully delete");
    }

}
