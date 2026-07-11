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
import sonaged.collecte.master.dto.Commune;
import sonaged.collecte.master.service.CommuneService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/communes")
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
    public Commune readCommune(@PathVariable("communeId") Long communeId){
        return communeService.readCommune(communeId);
    }

    @Operation(summary = "Get All Commune")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Communes"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("s")
    public List<Commune> getAllCommune(){
        return communeService.readAllCommune();
    }

    @Operation(summary = "Read Commune by pagination with size", description = "Read Commune")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "404", description = "Resource access does not exist"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")

    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<Commune> readAllCommune(@RequestParam("page") int page, @RequestParam("size") int size){
        Pageable pageable = PageRequest.of (page, size);
        return communeService.readAllCommune(pageable);
    }

    @Operation(summary = "Create one Commune")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one commune"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public Commune createCommune(@RequestBody Commune communeDto){
        return communeService.createCommune(communeDto);
    }

    @Operation(summary = "update One Commune by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one commune"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{communeId}")
    public Commune updateOneCommune(@PathVariable("communeId") Long communeId, @RequestBody() Commune commune) {
        return communeService.updateCommune(communeId, commune);
    }

    @Operation(summary = "Delete One Commune by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one commune"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{communeId}")
    public String deleteCommune(@PathVariable("communeId") Long communeId) {
        communeService.deleteCommune (communeId);
        return "Successfully delete";
    }
}
