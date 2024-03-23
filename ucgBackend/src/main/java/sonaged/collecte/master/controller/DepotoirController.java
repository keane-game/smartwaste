package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.DepotoirDto;
import sonaged.collecte.master.service.DepotoirService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/depotoir")
public class DepotoirController {
    private final DepotoirService depotoirService;

    @Operation(summary = "Get One Depotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Depotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{depotoirId}")
    public DepotoirDto getOneDepotoir(@PathVariable("depotoirId") Long depotoirId){
        return depotoirService.getOneDepotoir(depotoirId);
    }

    @Operation(summary = "Get All Depotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Depotoirs"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<DepotoirDto> getAllDepotoir(){
        return depotoirService.getAllDepotoir();
    }

    @Operation(summary = "Create one Depotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one depotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public DepotoirDto createOneDepotoir(@RequestBody DepotoirDto depotoirDto){
        return depotoirService.createOneDepotoir(depotoirDto);
    }

    @Operation(summary = "update One Depotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one depotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{depotoirId}")
    public DepotoirDto  updateOneDepotoir(@PathVariable("depotoirId") Long depotoirId, @RequestBody() DepotoirDto depotoirDto) {
        return depotoirService.updateOneDepotoir(depotoirId, depotoirDto);
    }

    @Operation(summary = "Delete One Depotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one depotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{depotoirId}")
    public String deleteOneDepotoir(@PathVariable("depotoirId") Long depotoirId) {
        depotoirService.deleteOneDepotoir(depotoirId);
        return "Successfully delete";
    }

}
