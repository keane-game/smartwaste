package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.TypeDepotoirDto;
import sonaged.collecte.master.service.TypeDepotoirService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/typedepotoir")
public class TypeDepotoirController {

    private final TypeDepotoirService typeDepotoirService;

    @Operation(summary = "Get One TypeDepotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One TypeDepotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{typeDepotoirId}")
    public TypeDepotoirDto getOneTypeDepotoir(@PathVariable("typeDepotoirId") Long typeDepotoirId){
        return typeDepotoirService.getOneTypeDepotoir(typeDepotoirId);
    }

    @Operation(summary = "Get All TypeDepotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All TypeDepotoirs"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<TypeDepotoirDto> getAllTypeDepotoir(){
        return typeDepotoirService.getAllTypeDepotoir();
    }

    @Operation(summary = "Create one TypeDepotoir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one typeDepotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public TypeDepotoirDto createOneTypeDepotoir(@RequestBody TypeDepotoirDto typeDepotoirDto){
        return typeDepotoirService.createOneTypeDepotoir(typeDepotoirDto);
    }

    @Operation(summary = "update One TypeDepotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one typeDepotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{typeDepotoirId}")
    public TypeDepotoirDto  updateOneTypeDepotoir(@PathVariable("typeDepotoirId") Long typeDepotoirId, @RequestBody() TypeDepotoirDto typeDepotoirDto) {
        return typeDepotoirService.updateOneTypeDepotoir(typeDepotoirId, typeDepotoirDto);
    }

    @Operation(summary = "Delete One TypeDepotoir by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one typeDepotoir"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{typeDepotoirId}")
    public String deleteOneTypeDepotoir(@PathVariable("typeDepotoirId") Long typeDepotoirId) {
        typeDepotoirService.deleteOneTypeDepotoir(typeDepotoirId);
        return "Successfully delete";
    }

}
