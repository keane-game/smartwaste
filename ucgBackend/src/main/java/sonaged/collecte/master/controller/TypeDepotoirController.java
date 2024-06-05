package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.TypeDepotoir;
import sonaged.collecte.master.service.TypeDepotoirService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/typedepotoirs")
public class TypeDepotoirController {

    private final TypeDepotoirService typeDepotoirService;

    @Operation(summary = "Get One TypeDepotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One TypeDepotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{typeDepotoirId}")
    public TypeDepotoir getOneTypeDepotoir(@PathVariable("typeDepotoirId") Long typeDepotoirId){
        return typeDepotoirService.readTypeDepotoir (typeDepotoirId);
    }

    @Operation(summary = "Get All TypeDepotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All TypeDepotoirs"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<TypeDepotoir> readAllTypeDepotoir(){
        return typeDepotoirService.readAllTypeDepotoir ();
    }

    @Operation(summary = "Create one TypeDepotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one typeDepotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public TypeDepotoir createTypeDepotoir(@RequestBody TypeDepotoir typeDepotoir){
        return typeDepotoirService.createTypeDepotoir (typeDepotoir);
    }

    @Operation(summary = "update One TypeDepotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one typeDepotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{typeDepotoirId}")
    public TypeDepotoir  updateTypeDepotoir(@PathVariable("typeDepotoirId") Long typeDepotoirId, @RequestBody() TypeDepotoir typeDepotoir) {
        return typeDepotoirService.updateTypeDepotoir (typeDepotoirId, typeDepotoir);
    }

    @Operation(summary = "Delete One TypeDepotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one typeDepotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{typeDepotoirId}")
    public String deleteTypeDepotoir(@PathVariable("typeDepotoirId") Long typeDepotoirId) {
        typeDepotoirService.deleteTypeDepotoir (typeDepotoirId);
        return "Successfully delete";
    }

}
