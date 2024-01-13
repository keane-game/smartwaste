package ucg.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ucg.collecte.master.dto.DepartementDto;
import ucg.collecte.master.service.DepartementService;

import java.util.List;

@RestController
@AllArgsConstructor
public class DepartementController {
    private final DepartementService departementService;

    @Operation(summary = "Get One Departement by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Departement"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/departement/{id}")
    public ResponseEntity<DepartementDto> getOneDepartement(@PathVariable("departementId") Long departementid){
        DepartementDto departementDto = departementService.getOneDepartement(departementid);
        return ResponseEntity
                .ok()
                .body(departementDto);
    }

    @Operation(summary = "Get All Departement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Departements"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/departements/all")
    public ResponseEntity<List<DepartementDto>> getAllDepartement(){
        return ResponseEntity
                .ok()
                .body(departementService.getAllDepartement());
    }

    @Operation(summary = "Create one Departement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one departement"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/create/departement")
    public ResponseEntity<DepartementDto> createOneDepartement(@RequestBody DepartementDto departementDto){
        DepartementDto departement = departementService.createOneDepartement(departementDto);
        return ResponseEntity.ok()
                .body(departement);
    }

    @Operation(summary = "update One Departement by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one departement"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/update/departement/{id}")
    public ResponseEntity<DepartementDto>  updateOneDepartement(@PathVariable("departementId") Long departementId, @RequestBody() DepartementDto departementDto) {
        DepartementDto departement = departementService.updateOneDepartement(departementId, departementDto);
        return ResponseEntity.ok()
                .body(departement);
    }

    @Operation(summary = "Delete One Departement by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one departement"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/departement/{id}")
    public ResponseEntity<String> deleteOneDepartement(@PathVariable("departementId") Long departementId) {
        departementService.deleteOneDepartement(departementId);
        return ResponseEntity.ok()
                .body("Successfully delete");
    }
}
