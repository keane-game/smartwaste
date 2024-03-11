package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.TypeDepotoirDto;
import sonaged.collecte.master.service.TypeDepotoirService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api")
public class TypeDepotoirController {

    private final TypeDepotoirService typeDepotoirService;

    @Operation(summary = "Get One TypeDepotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One TypeDepotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/typeDepotoir/{id}")
    public ResponseEntity<TypeDepotoirDto> getOneTypeDepotoir(@PathVariable("typeDepotoirId") Long typeDepotoirId){
        TypeDepotoirDto typeDepotoirDto = typeDepotoirService.getOneTypeDepotoir(typeDepotoirId);
        return ResponseEntity
                .ok()
                .body(typeDepotoirDto);
    }

    @Operation(summary = "Get All TypeDepotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All TypeDepotoirs"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/typeDepotoirs/all")
    public ResponseEntity<List<TypeDepotoirDto>> getAllTypeDepotoir(){
        return ResponseEntity
                .ok()
                .body(typeDepotoirService.getAllTypeDepotoir());
    }

    @Operation(summary = "Create one TypeDepotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one typeDepotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/create/typeDepotoir")
    public ResponseEntity<TypeDepotoirDto> createOneTypeDepotoir(@RequestBody TypeDepotoirDto typeDepotoirDto){
        TypeDepotoirDto typeDepotoir = typeDepotoirService.createOneTypeDepotoir(typeDepotoirDto);
        return ResponseEntity.ok()
                .body(typeDepotoir);
    }

    @Operation(summary = "update One TypeDepotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one typeDepotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/update/typeDepotoir/{id}")
    public ResponseEntity<TypeDepotoirDto>  updateOneTypeDepotoir(@PathVariable("typeDepotoirId") Long typeDepotoirId, @RequestBody() TypeDepotoirDto typeDepotoirDto) {
        TypeDepotoirDto typeDepotoir = typeDepotoirService.updateOneTypeDepotoir(typeDepotoirId, typeDepotoirDto);
        return ResponseEntity.ok()
                .body(typeDepotoir);
    }

    @Operation(summary = "Delete One TypeDepotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one typeDepotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/typeDepotoir/{id}")
    public ResponseEntity<String> deleteOneTypeDepotoir(@PathVariable("typeDepotoirId") Long typeDepotoirId) {
        typeDepotoirService.deleteOneTypeDepotoir(typeDepotoirId);
        return ResponseEntity.ok()
                .body("Successfully delete");
    }

}
