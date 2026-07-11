package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.Region;
import sonaged.collecte.master.service.RegionService;

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
    public Region readRegion(@PathVariable("regionId") Long regionId){
        return regionService.readRegion(regionId);
    }

    @Operation(summary = "Get All region")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All regions"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<Region> readAllRegion(){
        return regionService.readAllRegion();
    }

    @Operation(summary = "Create one region")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one region"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public Region createRegion(@RequestBody Region regionDto){
        return regionService.createRegion (regionDto);

    }
}
