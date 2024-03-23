package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.CommuneDto;
import sonaged.collecte.master.service.CommuneService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/commune")
public class CommuneController {

    private final CommuneService communeService;
    @Operation(summary = "Get One Commune by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Commune"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{communeId}")
    public CommuneDto getOneCommune(@PathVariable("communeId") Long communeid){
        return communeService.getOneCommune(communeid);
    }

    @Operation(summary = "Get All Commune")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Communes"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<CommuneDto> getAllCommune(){
        return communeService.getAllCommune();
    }

    @Operation(summary = "Create one Commune")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one commune"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public CommuneDto createOneCommune(@RequestBody CommuneDto communeDto){
        return communeService.createOneCommune(communeDto);
    }

    @Operation(summary = "update One Commune by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one commune"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{communeId}")
    public CommuneDto updateOneCommune(@PathVariable("communeId") Long communeId, @RequestBody() CommuneDto communeDto) {
        return communeService.updateOneCommune(communeId, communeDto);
    }

    @Operation(summary = "Delete One Commune by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one commune"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{communeId}")
    public String deleteOneCommune(@PathVariable("communeId") Long communeId) {
        communeService.deleteOneCommune(communeId);
        return "Successfully delete";
    }
}
