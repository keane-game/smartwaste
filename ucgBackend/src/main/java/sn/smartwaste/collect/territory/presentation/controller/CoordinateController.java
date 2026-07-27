package sn.smartwaste.collect.territory.presentation.controller;

import java.util.UUID;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.smartwaste.collect.territory.application.dto.Coordinate;
import sn.smartwaste.collect.territory.application.service.CoordinateService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/coordinates")
public class CoordinateController {
    private final CoordinateService coordinateService;

    @Operation(summary = "Get One Coordinate by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Coordinate"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{coordinateId}")
    public Coordinate readCoordinate(@PathVariable("coordinateId") UUID coordinateId){
       return coordinateService.readCoordinate(coordinateId);
    }


    @Operation(summary = "Get All Coordinate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Coordinates"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<Coordinate> readAllCoordinate(){
        return coordinateService.readAllCoordinate();
    }

    @Operation(summary = "Create one Coordinate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one coordinate"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public Coordinate createCoordinate(@RequestBody Coordinate coordinateDto){
        return coordinateService.createCoordinate(coordinateDto);

    }

    @Operation(summary = "update One Coordinate by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one coordinate"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{coordinateId}")
    public Coordinate  updateCoordinate(@PathVariable("coordinateId") UUID coordinateId, @RequestBody() Coordinate coordinateDto) {
        return coordinateService.updateCoordinate(coordinateId, coordinateDto);

    }

    @Operation(summary = "Delete One Coordinate by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one coordinate"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/coordinate/{coordinateId}")
    public String deleteCoordinate(@PathVariable("coordinateId") UUID coordinateId) {
        coordinateService.deleteCoordinate(coordinateId);
        return "Successfully delete";
    }
}
