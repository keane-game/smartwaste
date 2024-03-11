package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.GeometryDto;
import sonaged.collecte.master.service.GeometryService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api")
public class GeometryController {
    private final GeometryService geometryService;

    @Operation(summary = "Get One Geometry by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Geometry"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/geometry/{geometryId}")
    public ResponseEntity<GeometryDto> getOneGeometry(@PathVariable("geometryId") Long geometryId){
        GeometryDto geometryDto = geometryService.getOneGeometry(geometryId);
        return ResponseEntity
                .ok()
                .body(geometryDto);
    }

    @Operation(summary = "Get All Geometries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Geometries"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/geometry/all")
    public ResponseEntity<List<GeometryDto>> getAllGeometry(){
        return ResponseEntity
                .ok()
                .body(geometryService.getAllGeometry());
    }

    @Operation(summary = "Create one Geometry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one geometry"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/create/geometry")
    public ResponseEntity<GeometryDto> createOneGeometry(@RequestBody GeometryDto geometrydto){
        GeometryDto geometry = geometryService.createOneGeometry(geometrydto);
        return ResponseEntity.ok()
                .body(geometry);
    }

    @Operation(summary = "update One Geometry by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one geometry"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/update/geometry/{geometryId}")
    public ResponseEntity<GeometryDto>  updateOneGeometry(@PathVariable("geometryId") Long geometryId, @RequestBody() GeometryDto geometryDto) {
        GeometryDto geometry = geometryService.updateOneGeometry(geometryId, geometryDto);
        return ResponseEntity.ok()
                .body(geometry);
    }

    @Operation(summary = "Delete One Geometry by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one geometry"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/geometry/{geometryId}")
    public ResponseEntity<String> deleteOneGeometry(@PathVariable("geometryId") Long geometryId) {
        geometryService.deleteOneGeometry(geometryId);
        return ResponseEntity.ok()
                .body("Successfully delete");
    }
}
