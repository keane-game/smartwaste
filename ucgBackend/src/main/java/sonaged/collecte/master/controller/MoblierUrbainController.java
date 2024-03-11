package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.MoblierUrbainDto;
import sonaged.collecte.master.service.MoblierUrbainService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api")
public class MoblierUrbainController {
    private final MoblierUrbainService moblierUrbainService;
    @Operation(summary = "Get One MoblierUrbain by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One MoblierUrbain"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/moblier-urbain/{moblierUrbainId}")
    public ResponseEntity<MoblierUrbainDto> getOneMoblierUrbain(@PathVariable("moblierUrbainId") Long moblierUrbainid){
        MoblierUrbainDto moblierUrbainDto = moblierUrbainService.getOneMoblierUrbain(moblierUrbainid);
        return ResponseEntity
                .ok()
                .body(moblierUrbainDto);
    }

    @Operation(summary = "Get All MoblierUrbain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All MoblierUrbains"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/moblier-urbain/all")
    public ResponseEntity<List<MoblierUrbainDto>> getAllMoblierUrbain(){
        return ResponseEntity
                .ok()
                .body(moblierUrbainService.getAllMoblierUrbain());
    }

    @Operation(summary = "Create one MoblierUrbain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one moblierUrbain"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/create/moblier-urbain")
    public ResponseEntity<MoblierUrbainDto> createOneMoblierUrbain(@RequestBody MoblierUrbainDto moblierUrbainDto){
        MoblierUrbainDto moblierUrbain = moblierUrbainService.createOneMoblierUrbain(moblierUrbainDto);
        return ResponseEntity.ok()
                .body(moblierUrbain);
    }

    @Operation(summary = "update One MoblierUrbain by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one moblierUrbain"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/update/moblier-urbain/{moblierUrbainId}")
    public ResponseEntity<MoblierUrbainDto>  updateOneMoblierUrbain(@PathVariable("moblierUrbainId") Long moblierUrbainId, @RequestBody() MoblierUrbainDto moblierUrbainDto) {
        MoblierUrbainDto moblierUrbain = moblierUrbainService.updateOneMoblierUrbain(moblierUrbainId, moblierUrbainDto);
        return ResponseEntity.ok()
                .body(moblierUrbain);
    }

    @Operation(summary = "Delete One MoblierUrbain by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one moblierUrbain"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/moblier-urbain/{moblierUrbainId}")
    public ResponseEntity<String> deleteOneMoblierUrbain(@PathVariable("moblierUrbainId") Long moblierUrbainId) {
        moblierUrbainService.deleteOneMoblierUrbain(moblierUrbainId);
        return ResponseEntity.ok()
                .body("Successfully delete");
    }
}
