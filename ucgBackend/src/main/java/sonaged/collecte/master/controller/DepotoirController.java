package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.Depotoir;
import sonaged.collecte.master.service.DepotoirService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/depotoirs")
public class DepotoirController {
    private final DepotoirService depotoirService;

    @Operation(summary = "Read Depotoir by pagination with size", description = "Read Depotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "404", description = "Resource access does not exist"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")

    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{depotoirId}")
    public Depotoir readDepotoir(@PathVariable("depotoirId") Long depotoirId){
        return depotoirService.readDepotoir(depotoirId);
    }

    @Operation(summary = "Read Depotoir by pagination with size", description = "Read Depotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "404", description = "Resource access does not exist"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")

    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("s")
    public List<Depotoir> readAllDepotoir(){
        return depotoirService.readAllDepotoir();
    }

    @Operation(summary = "Read Depotoir by pagination with size", description = "Read Depotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "404", description = "Resource access does not exist"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")

    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<Depotoir> readAllDepotoir(@RequestParam("page") int page, @RequestParam("size") int size){
        Pageable pageable = PageRequest.of (page, size);
        return depotoirService.readAllDepotoir(pageable);
    }

    @Operation(summary = "Create one Depotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one depotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public Depotoir createOneDepotoir(@RequestBody Depotoir depotoir){
        return depotoirService.createDepotoir(depotoir);
    }

    @Operation(summary = "update One Depotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one depotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{depotoirId}")
    public Depotoir  updateDepotoir(@PathVariable("depotoirId") Long depotoirId, @RequestBody() Depotoir depotoir) {
        return depotoirService.updateDepotoir(depotoirId, depotoir);
    }

    @Operation(summary = "Delete One Depotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one depotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{depotoirId}")
    public String deleteDepotoir(@PathVariable("depotoirId") Long depotoirId) {
        depotoirService.deleteDepotoir (depotoirId);
        return "Successfully delete";
    }

}
