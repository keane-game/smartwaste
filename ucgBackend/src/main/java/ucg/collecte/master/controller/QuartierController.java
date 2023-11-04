package ucg.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ucg.collecte.master.dto.QuartierDto;
import ucg.collecte.master.service.QuartierService;

import java.util.List;

@RestController
@AllArgsConstructor
public class QuartierController {


    private final QuartierService quartierService;
    @Operation(summary = "Get One Quartier by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Quartier"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/quartier/{id}")
    public ResponseEntity<QuartierDto> getOneQuartier(@PathVariable("quartierId") Long quartierId){
        QuartierDto quartierDto = quartierService.getOneQuartier(quartierId);
        return ResponseEntity
                .ok()
                .body(quartierDto);
    }

    @Operation(summary = "Get All Quartier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Quartiers"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/quartiers/all")
    public ResponseEntity<List<QuartierDto>> getAllQuartier(){
        return ResponseEntity
                .ok()
                .body(quartierService.getAllQuartier());
    }

    @Operation(summary = "Create one Quartier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one quartier"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/create/quartier")
    public ResponseEntity<QuartierDto> createOneQuartier(@RequestBody QuartierDto quartierDto){
        QuartierDto quartier = quartierService.createOneQuartier(quartierDto);
        return ResponseEntity.ok()
                .body(quartier);
    }

    @Operation(summary = "update One Quartier by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one quartier"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/update/quartier/{id}")
    public ResponseEntity<QuartierDto>  updateOneQuartier(@PathVariable("quartierId") Long quartierId, @RequestBody() QuartierDto quartierDto) {
        QuartierDto quartier = quartierService.updateOneQuartier(quartierId, quartierDto);
        return ResponseEntity.ok()
                .body(quartier);
    }

    @Operation(summary = "Delete One Quartier by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one quartier"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/quartier/{id}")
    public ResponseEntity<String> deleteOneQuartier(@PathVariable("quartierId") Long quartierId) {
        quartierService.deleteOneQuartier(quartierId);
        return ResponseEntity.ok()
                .body("Successfully delete");
    }
}
