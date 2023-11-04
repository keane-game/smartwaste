package ucg.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import ucg.collecte.master.dto.RegionDto;
import ucg.collecte.master.service.RegionService;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class RegionController {


    private final RegionService regionService;
    @Operation(summary = "Get One region by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One region"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/region/{id}")
    public ResponseEntity<RegionDto> getOneRegion(@PathVariable("regionId") Long regionid){
        RegionDto regionDto = regionService.getOneRegion(regionid);
        return ResponseEntity
                .ok()
                .body(regionDto);
    }

    @Operation(summary = "Get All region")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All regions"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/regions/all")
    public ResponseEntity<List<RegionDto>> getAllRegion(){
        return ResponseEntity
                .ok()
                .body(regionService.getAllRegion());
    }

    @Operation(summary = "Create one region")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one region"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/create/region")
    public ResponseEntity<RegionDto> createOneRegion(@RequestBody RegionDto regionDto){
        RegionDto region = regionService.createOneRegion(regionDto);
        return ResponseEntity.ok()
                .body(region);
    }
}
