package sn.smartwaste.collect.territory.presentation.controller;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.smartwaste.collect.territory.application.dto.Region;
import sn.smartwaste.collect.territory.application.service.RegionService;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/v1/regions")
public class RegionController {


    private final RegionService regionService;

    @Operation(summary = "Get One region by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One region"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{regionId}")
    public Region readRegion(@PathVariable("regionId") UUID regionId){
        return regionService.readRegion(regionId);
    }

    @Operation(summary = "Get All region")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All regions"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("s")
    public List<Region> readAllRegion(){
        return regionService.readAllRegion();
    }

    /**
     * Corrige une incohérence relevée par audit (2026-08-10, `docs/FRONTEND_API_MAPPING.md`) : cette
     * ressource n'avait qu'une liste plate. Même patron que Commune/Quartier/Depotoir/User/Alert.
     */
    @Operation(summary = "Read region by pagination with size", description = "Read regions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<Region> readAllRegion(@RequestParam("page") int page, @RequestParam("size") int size){
        Pageable pageable = PageRequest.of(page, size);
        return regionService.readAllRegion(pageable);
    }

    @Operation(summary = "Create one region")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one region"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Region createRegion(@RequestBody Region regionDto){
        return regionService.createRegion (regionDto);

    }

    /**
     * Corrige un défaut relevé par audit (2026-08-10, `docs/FRONTEND_API_MAPPING.md`) : la région
     * était le seul niveau territorial sans mise à jour ni suppression exposées, alors que le
     * service les portait déjà — en stub jamais écrit (`return null` / corps vide).
     */
    @Operation(summary = "update One region by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one region"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{regionId}")
    public Region updateRegion(@PathVariable("regionId") UUID regionId, @RequestBody Region regionDto) {
        return regionService.updateRegion(regionId, regionDto);
    }

    @Operation(summary = "Delete One region by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one region"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{regionId}")
    public String deleteRegion(@PathVariable("regionId") UUID regionId) {
        regionService.deleteRegion(regionId);
        return "Successfully delete";
    }
}
