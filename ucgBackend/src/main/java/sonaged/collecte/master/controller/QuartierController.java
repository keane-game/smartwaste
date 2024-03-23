package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.QuartierDto;
import sonaged.collecte.master.service.QuartierService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/quartier")
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
    public QuartierDto getOneQuartier(@PathVariable("quartierId") Long quartierId){
        return quartierService.getOneQuartier(quartierId);
    }

    @Operation(summary = "Get All Quartier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Quartiers"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<QuartierDto> getAllQuartier(){
        return quartierService.getAllQuartier();
    }

    @Operation(summary = "Create one Quartier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one quartier"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public QuartierDto createOneQuartier(@RequestBody QuartierDto quartierDto){
        return quartierService.createOneQuartier(quartierDto);
    }

    @Operation(summary = "update One Quartier by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one quartier"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{quartierId}")
    public QuartierDto updateOneQuartier(@PathVariable("quartierId") Long quartierId, @RequestBody() QuartierDto quartierDto) {
        return quartierService.updateOneQuartier(quartierId, quartierDto);
    }

    @Operation(summary = "Delete One Quartier by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one quartier"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{quartierId}")
    public String deleteOneQuartier(@PathVariable("quartierId") Long quartierId) {
        quartierService.deleteOneQuartier(quartierId);
        return "Successfully delete";
    }
}
