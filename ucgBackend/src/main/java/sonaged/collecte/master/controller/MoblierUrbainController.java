package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.MoblierUrbain;
import sonaged.collecte.master.service.MoblierUrbainService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/moblier-urbains")
public class MoblierUrbainController {

    private final MoblierUrbainService moblierUrbainService;

    @Operation(summary = "Get One MoblierUrbain by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One MoblierUrbain"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{moblierUrbainId}")
    public ResponseEntity<MoblierUrbain> readMoblierUrbain(@PathVariable("moblierUrbainId") Long moblierUrbainid){
        MoblierUrbain moblierUrbain = moblierUrbainService.readMoblierUrbain (moblierUrbainid);
        return ResponseEntity
                .ok()
                .body(moblierUrbain);
    }

    @Operation(summary = "Get All MoblierUrbain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All MoblierUrbains"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<MoblierUrbain> readAllMoblierUrbain(){
        return moblierUrbainService.readAllMoblierUrbain();
    }

    @Operation(summary = "Create one MoblierUrbain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one moblierUrbain"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public MoblierUrbain createMoblierUrbain(@RequestBody MoblierUrbain moblierUrbain){
        return moblierUrbainService.createMoblierUrbain (moblierUrbain);

    }

    @Operation(summary = "update One MoblierUrbain by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one moblierUrbain"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{moblierUrbainId}")
    public MoblierUrbain  updateMoblierUrbain(@PathVariable("moblierUrbainId") Long moblierUrbainId, @RequestBody() MoblierUrbain moblierUrbain) {
        return moblierUrbainService.updateMoblierUrbain (moblierUrbainId, moblierUrbain);

    }

    @Operation(summary = "Delete One MoblierUrbain by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one moblierUrbain"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{moblierUrbainId}")
    public String deleteMoblierUrbain(@PathVariable("moblierUrbainId") Long moblierUrbainId) {
        moblierUrbainService.deleteMoblierUrbain (moblierUrbainId);
        return "Successfully delete";
    }
}
