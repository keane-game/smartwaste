package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.Geometry;
import sonaged.collecte.master.service.GeometryService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/geometries")
public class GeometryController {
    private final GeometryService geometryService;

    @Operation(summary = "Get One Geometry by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Geometry"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{geometryId}")
    public Geometry readGeometry(@PathVariable("geometryId") Long geometryId){
        return geometryService.readGeometry(geometryId);

    }

    @Operation(summary = "Get All Geometries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Geometries"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<Geometry> readAllGeometry(){
        return geometryService.readAllGeometry();
    }

    @Operation(summary = "Create one Geometry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one geometry"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public Geometry createGeometry(@RequestBody Geometry geometry){
        return geometryService.createGeometry(geometry);

    }

    @Operation(summary = "update One Geometry by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one geometry"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{geometryId}")
    public Geometry  updateGeometry(@PathVariable("geometryId") Long geometryId, @RequestBody() Geometry geometry) {
        return geometryService.updateGeometry(geometryId, geometry);

    }

    @Operation(summary = "Delete One Geometry by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one geometry"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{geometryId}")
    public String deleteGeometry(@PathVariable("geometryId") Long geometryId) {
        geometryService.deleteGeometry (geometryId);
        return "Successfully delete";
    }
}
