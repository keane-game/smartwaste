package sn.smartwaste.collect.waste.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.smartwaste.collect.waste.application.dto.TypeDepotoir;
import sn.smartwaste.collect.waste.application.service.TypeDepotoirService;

import java.util.List;
import java.util.UUID;

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
    public TypeDepotoir getOneTypeDepotoir(@PathVariable("typeDepotoirId") UUID typeDepotoirId){
        return typeDepotoirService.readTypeDepotoir (typeDepotoirId);
    }

    @Operation(summary = "Get All TypeDepotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All TypeDepotoirs"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("s")
    public List<TypeDepotoir> readAllTypeDepotoir(){
        return typeDepotoirService.readAllTypeDepotoir ();
    }

    /**
     * Corrige une incohérence relevée par audit (2026-08-10, `docs/FRONTEND_API_MAPPING.md`) : cette
     * ressource n'avait qu'une liste plate. Même patron que Commune/Quartier/Depotoir/User/Alert.
     */
    @Operation(summary = "Read TypeDepotoir by pagination with size", description = "Read TypeDepotoirs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<TypeDepotoir> readAllTypeDepotoir(@RequestParam("page") int page, @RequestParam("size") int size){
        Pageable pageable = PageRequest.of(page, size);
        return typeDepotoirService.readAllTypeDepotoir(pageable);
    }

    @Operation(summary = "Create one TypeDepotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one typeDepotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
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
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{typeDepotoirId}")
    public TypeDepotoir  updateTypeDepotoir(@PathVariable("typeDepotoirId") UUID typeDepotoirId, @RequestBody() TypeDepotoir typeDepotoir) {
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
    public String deleteTypeDepotoir(@PathVariable("typeDepotoirId") UUID typeDepotoirId) {
        typeDepotoirService.deleteTypeDepotoir (typeDepotoirId);
        return "Successfully delete";
    }

}
