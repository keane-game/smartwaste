package sn.smartwaste.collect.waste.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.smartwaste.collect.waste.application.dto.Depotoir;
import sn.smartwaste.collect.waste.application.service.DepotoirService;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/depotoirs")
public class DepotoirController {
    private final DepotoirService depotoirService;

    @Operation(summary = "Read Depotoir by pagination with size", description = "Read Depotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "404", description = "Resource access does not exist"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")

    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{depotoirId}")
    public Depotoir readDepotoir(@PathVariable("depotoirId") UUID depotoirId){
        return depotoirService.readDepotoir(depotoirId);
    }

    @Operation(summary = "Read Depotoir by pagination with size", description = "Read Depotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "404", description = "Resource access does not exist"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")

    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("s")
    public List<Depotoir> readAllDepotoir(){
        return depotoirService.readAllDepotoir();
    }

    @Operation(summary = "Read Depotoir by pagination with size", description = "Read Depotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "404", description = "Resource access does not exist"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")

    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<Depotoir> readAllDepotoir(@RequestParam("page") int page, @RequestParam("size") int size){
        Pageable pageable = PageRequest.of (page, size);
        return depotoirService.readAllDepotoir(pageable);
    }

    @Operation(summary = "Create one Depotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one depotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Depotoir createOneDepotoir(@RequestBody Depotoir depotoir){
        return depotoirService.createDepotoir(depotoir);
    }

    @Operation(summary = "update One Depotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one depotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{depotoirId}")
    public Depotoir  updateDepotoir(@PathVariable("depotoirId") UUID depotoirId, @RequestBody() Depotoir depotoir) {
        return depotoirService.updateDepotoir(depotoirId, depotoir);
    }

    @Operation(summary = "Soft-delete One Depotoir by Id",
            description = "Suppression logique : le dépotoir passe en attente de suppression (purge après rétention).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Soft-delete one depotoir"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{depotoirId}")
    public String deleteDepotoir(@PathVariable("depotoirId") UUID depotoirId) {
        depotoirService.deleteDepotoir (depotoirId);
        return "Successfully soft-deleted";
    }

    @Operation(summary = "List depotoirs pending deletion",
            description = "Dépotoirs en attente de suppression (avec date de purge prévue).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/deletions")
    public List<Depotoir> readPendingDeletions() {
        return depotoirService.readPendingDeletions();
    }

    @Operation(summary = "Restore a soft-deleted Depotoir",
            description = "Restaure un dépotoir en attente de suppression (si le délai de rétention n'est pas dépassé).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Restored"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Not pending deletion / retention delay elapsed"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{depotoirId}/restore")
    public Depotoir restoreDepotoir(@PathVariable("depotoirId") UUID depotoirId) {
        return depotoirService.restoreDepotoir(depotoirId);
    }

}
