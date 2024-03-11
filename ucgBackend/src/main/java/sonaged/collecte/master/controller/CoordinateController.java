package sonaged.collecte.master.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.CoordinateDto;
import sonaged.collecte.master.service.CoordinateService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api")
public class CoordinateController {
    private final CoordinateService coordinateService;

    @Operation(summary = "Get One Coordinate by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Coordinate"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/coordinate/{coordinateId}")
    public ResponseEntity<CoordinateDto> getOneCoordinate(@PathVariable("coordinateId") Long coordinateId){
        CoordinateDto coordinateDto = coordinateService.getOneCoordinate(coordinateId);
        return ResponseEntity
                .ok()
                .body(coordinateDto);
    }


    @Operation(summary = "Get All Coordinate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Coordinates"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/coordinates/all")
    public ResponseEntity<List<CoordinateDto>> getAllCoordinate(){
        return ResponseEntity
                .ok()
                .body(coordinateService.getAllCoordinate());
    }

    @Operation(summary = "Create one Coordinate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one coordinate"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/create/coordinate")
    public ResponseEntity<CoordinateDto> createOneCoordinate(@RequestBody CoordinateDto coordinateDto){
        CoordinateDto coordinate = coordinateService.createOneCoordinate(coordinateDto);
        return ResponseEntity.ok()
                .body(coordinate);
    }

    @Operation(summary = "update One Coordinate by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one coordinate"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/update/coordinate/{coordinateId}")
    public ResponseEntity<CoordinateDto>  updateOneCoordinate(@PathVariable("coordinateId") Long coordinateId, @RequestBody() CoordinateDto coordinateDto) {
        CoordinateDto coordinate = coordinateService.updateOneCoordinate(coordinateId, coordinateDto);
        return ResponseEntity.ok()
                .body(coordinate);
    }

    @Operation(summary = "Delete One Coordinate by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one coordinate"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/coordinate/{coordinateId}")
    public ResponseEntity<String> deleteOneCoordinate(@PathVariable("coordinateId") Long coordinateId) {
        coordinateService.deleteOneCoordinate(coordinateId);
        return ResponseEntity.ok()
                .body("Successfully delete");
    }
}
