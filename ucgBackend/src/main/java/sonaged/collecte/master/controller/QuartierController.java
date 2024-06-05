package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.Quartier;
import sonaged.collecte.master.service.QuartierService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/quartiers")
public class QuartierController {


    private final QuartierService quartierService;
    @Operation(summary = "Get One Quartier by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Quartier"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{quartierId}")
    public Quartier readQuartier(@PathVariable("quartierId") Long quartierId){
        return quartierService.readQuartier(quartierId);
    }

    @Operation(summary = "Get All Quartier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Quartiers"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<Quartier> readAllQuartier(){
        return quartierService.readAllQuartier ();
    }

    @Operation(summary = "Create one Quartier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one quartier"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public Quartier createQuartier(@RequestBody Quartier quartier){
        return quartierService.createQuartier (quartier);
    }

    @Operation(summary = "update One Quartier by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one quartier"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{quartierId}")
    public Quartier updateQuartier(@PathVariable("quartierId") Long quartierId, @RequestBody() Quartier quartier) {
        return quartierService.updateQuartier (quartierId, quartier);
    }

    @Operation(summary = "Delete One Quartier by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one quartier"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{quartierId}")
    public String deleteQuartier(@PathVariable("quartierId") Long quartierId) {
        quartierService.deleteQuartier (quartierId);
        return "Successfully delete";
    }
}
