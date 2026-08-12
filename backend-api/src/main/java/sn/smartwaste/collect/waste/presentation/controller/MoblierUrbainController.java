package sn.smartwaste.collect.waste.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.smartwaste.collect.waste.application.dto.MoblierUrbain;
import sn.smartwaste.collect.waste.application.service.MoblierUrbainService;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/moblier-urbains")
public class MoblierUrbainController {

    private final MoblierUrbainService moblierUrbainService;

    @Operation(summary = "Get One MoblierUrbain by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One MoblierUrbain"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{moblierUrbainId}")
    public ResponseEntity<MoblierUrbain> readMoblierUrbain(@PathVariable("moblierUrbainId") UUID moblierUrbainid){
        MoblierUrbain moblierUrbain = moblierUrbainService.readMoblierUrbain (moblierUrbainid);
        return ResponseEntity
                .ok()
                .body(moblierUrbain);
    }

    @Operation(summary = "Get All MoblierUrbain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All MoblierUrbains"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("s")
    public List<MoblierUrbain> readAllMoblierUrbain(){
        return moblierUrbainService.readAllMoblierUrbain();
    }

    /**
     * Corrige une incohérence relevée par audit (2026-08-10, `docs/FRONTEND_API_MAPPING.md`) : cette
     * ressource n'avait qu'une liste plate. Même patron que Commune/Quartier/Depotoir/User/Alert.
     */
    @Operation(summary = "Read MoblierUrbain by pagination with size", description = "Read MoblierUrbains")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<MoblierUrbain> readAllMoblierUrbain(@RequestParam("page") int page, @RequestParam("size") int size){
        Pageable pageable = PageRequest.of(page, size);
        return moblierUrbainService.readAllMoblierUrbain(pageable);
    }

    @Operation(summary = "Create one MoblierUrbain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one moblierUrbain"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public MoblierUrbain createMoblierUrbain(@RequestBody MoblierUrbain moblierUrbain){
        return moblierUrbainService.createMoblierUrbain (moblierUrbain);

    }

    @Operation(summary = "update One MoblierUrbain by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one moblierUrbain"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{moblierUrbainId}")
    public MoblierUrbain  updateMoblierUrbain(@PathVariable("moblierUrbainId") UUID moblierUrbainId, @RequestBody() MoblierUrbain moblierUrbain) {
        return moblierUrbainService.updateMoblierUrbain (moblierUrbainId, moblierUrbain);

    }

    @Operation(summary = "Delete One MoblierUrbain by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one moblierUrbain"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{moblierUrbainId}")
    public String deleteMoblierUrbain(@PathVariable("moblierUrbainId") UUID moblierUrbainId) {
        moblierUrbainService.deleteMoblierUrbain (moblierUrbainId);
        return "Successfully delete";
    }
}
