package sn.smartwaste.collect.territory.presentation.controller;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.smartwaste.collect.territory.application.dto.Quartier;
import sn.smartwaste.collect.territory.application.service.QuartierService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/quartiers")
public class QuartierController {


    private final QuartierService quartierService;
    @Operation(summary = "Get One Quartier by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Quartier"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{quartierId}")
    public Quartier readQuartier(@PathVariable("quartierId") UUID quartierId){
        return quartierService.readQuartier(quartierId);
    }

    @Operation(summary = "Read quartier by pagination with size", description = "Read quartiers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "404", description = "Resource access does not exist"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")

    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("s")
    public List<Quartier> readAllQuartier(){
        return quartierService.readAllQuartier ();
    }

    @Operation(summary = "Read quartier by pagination with size", description = "Read quartiers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "404", description = "Resource access does not exist"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")

    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<Quartier> readAllQuartiers(@RequestParam("page") int page, @RequestParam("size") int size){
        Pageable pageable = PageRequest.of (page, size);
        return quartierService.readAllQuartier (pageable);
    }

    @Operation(summary = "Create one Quartier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one quartier"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Quartier createQuartier(@RequestBody Quartier quartier){
        return quartierService.createQuartier (quartier);
    }

    @Operation(summary = "update One Quartier by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one quartier"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{quartierId}")
    public Quartier updateQuartier(@PathVariable("quartierId") UUID quartierId, @RequestBody() Quartier quartier) {
        return quartierService.updateQuartier (quartierId, quartier);
    }

    @Operation(summary = "Delete One Quartier by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one quartier"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{quartierId}")
    public String deleteQuartier(@PathVariable("quartierId") UUID quartierId) {
        quartierService.deleteQuartier (quartierId);
        return "Successfully delete";
    }
}
