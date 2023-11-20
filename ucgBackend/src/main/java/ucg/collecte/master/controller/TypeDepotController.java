package ucg.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ucg.collecte.master.dto.TypeDepotDto;
import ucg.collecte.master.service.TypeDepotService;

import java.util.List;

@RestController
@AllArgsConstructor
public class TypeDepotController {

    private final TypeDepotService typeDepotService;

    @Operation(summary = "Get One TypeDepot by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One TypeDepot"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/typeDepot/{id}")
    public ResponseEntity<TypeDepotDto> getOneTypeDepot(@PathVariable("typeDepotId") Long typeDepotId){
        TypeDepotDto typeDepotDto = typeDepotService.getOneTypeDepot(typeDepotId);
        return ResponseEntity
                .ok()
                .body(typeDepotDto);
    }

    @Operation(summary = "Get All TypeDepot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All TypeDepots"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/typeDepots/all")
    public ResponseEntity<List<TypeDepotDto>> getAllTypeDepot(){
        return ResponseEntity
                .ok()
                .body(typeDepotService.getAllTypeDepot());
    }

    @Operation(summary = "Create one TypeDepot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one typeDepot"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/create/typeDepot")
    public ResponseEntity<TypeDepotDto> createOneTypeDepot(@RequestBody TypeDepotDto typeDepotDto){
        TypeDepotDto typeDepot = typeDepotService.createOneTypeDepot(typeDepotDto);
        return ResponseEntity.ok()
                .body(typeDepot);
    }

    @Operation(summary = "update One TypeDepot by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one typeDepot"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/update/typeDepot/{id}")
    public ResponseEntity<TypeDepotDto>  updateOneTypeDepot(@PathVariable("typeDepotId") Long typeDepotId, @RequestBody() TypeDepotDto typeDepotDto) {
        TypeDepotDto typeDepot = typeDepotService.updateOneTypeDepot(typeDepotId, typeDepotDto);
        return ResponseEntity.ok()
                .body(typeDepot);
    }

    @Operation(summary = "Delete One TypeDepot by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one typeDepot"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/typeDepot/{id}")
    public ResponseEntity<String> deleteOneTypeDepot(@PathVariable("typeDepotId") Long typeDepotId) {
        typeDepotService.deleteOneTypeDepot(typeDepotId);
        return ResponseEntity.ok()
                .body("Successfully delete");
    }

}
