package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.DepotoirDto;
import sonaged.collecte.master.service.DepotoirService;

import java.util.List;

@RestController
@AllArgsConstructor
public class DepotoirController {
    private final DepotoirService depotoirService;

    @Operation(summary = "Get One Depotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Depotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/depotoir/{id}")
    public ResponseEntity<DepotoirDto> getOneDepotoir(@PathVariable("depotoirId") Long depotoirid){
        DepotoirDto depotoirDto = depotoirService.getOneDepotoir(depotoirid);
        return ResponseEntity
                .ok()
                .body(depotoirDto);
    }

    @Operation(summary = "Get All Depotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Depotoirs"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/depotoirs/all")
    public ResponseEntity<List<DepotoirDto>> getAllDepotoir(){
        return ResponseEntity
                .ok()
                .body(depotoirService.getAllDepotoir());
    }

    @Operation(summary = "Create one Depotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one depotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/create/depotoir")
    public ResponseEntity<DepotoirDto> createOneDepotoir(@RequestBody DepotoirDto depotoirDto){
        DepotoirDto depotoir = depotoirService.createOneDepotoir(depotoirDto);
        return ResponseEntity.ok()
                .body(depotoir);
    }

    @Operation(summary = "update One Depotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one depotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/update/depotoir/{id}")
    public ResponseEntity<DepotoirDto>  updateOneDepotoir(@PathVariable("depotoirId") Long depotoirId, @RequestBody() DepotoirDto depotoirDto) {
        DepotoirDto depotoir = depotoirService.updateOneDepotoir(depotoirId, depotoirDto);
        return ResponseEntity.ok()
                .body(depotoir);
    }

    @Operation(summary = "Delete One Depotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one depotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/depotoir/{id}")
    public ResponseEntity<String> deleteOneDepotoir(@PathVariable("depotoirId") Long depotoirId) {
        depotoirService.deleteOneDepotoir(depotoirId);
        return ResponseEntity.ok()
                .body("Successfully delete");
    }

}
