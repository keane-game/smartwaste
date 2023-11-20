package ucg.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ucg.collecte.master.dto.MobilieUrbainDto;
import ucg.collecte.master.service.MobilieUrbainService;

import java.util.List;

@RestController
@AllArgsConstructor
public class MobilieUrbainController {
    private final MobilieUrbainService mobilieUrbainService;
    @Operation(summary = "Get One MobilieUrbain by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One MobilieUrbain"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/mobilie-urbain/{id}")
    public ResponseEntity<MobilieUrbainDto> getOneMobilieUrbain(@PathVariable("mobilieUrbainId") Long mobilieUrbainid){
        MobilieUrbainDto mobilieUrbainDto = mobilieUrbainService.getOneMobilieUrbain(mobilieUrbainid);
        return ResponseEntity
                .ok()
                .body(mobilieUrbainDto);
    }

    @Operation(summary = "Get All MobilieUrbain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All MobilieUrbains"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/mobilie-urbain/all")
    public ResponseEntity<List<MobilieUrbainDto>> getAllMobilieUrbain(){
        return ResponseEntity
                .ok()
                .body(mobilieUrbainService.getAllMobilieUrbain());
    }

    @Operation(summary = "Create one MobilieUrbain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one mobilieUrbain"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/create/mobilie-urbain")
    public ResponseEntity<MobilieUrbainDto> createOneMobilieUrbain(@RequestBody MobilieUrbainDto mobilieUrbainDto){
        MobilieUrbainDto mobilieUrbain = mobilieUrbainService.createOneMobilieUrbain(mobilieUrbainDto);
        return ResponseEntity.ok()
                .body(mobilieUrbain);
    }

    @Operation(summary = "update One MobilieUrbain by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one mobilieUrbain"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/update/mobilie-urbain/{id}")
    public ResponseEntity<MobilieUrbainDto>  updateOneMobilieUrbain(@PathVariable("mobilieUrbainId") Long mobilieUrbainId, @RequestBody() MobilieUrbainDto mobilieUrbainDto) {
        MobilieUrbainDto mobilieUrbain = mobilieUrbainService.updateOneMobilieUrbain(mobilieUrbainId, mobilieUrbainDto);
        return ResponseEntity.ok()
                .body(mobilieUrbain);
    }

    @Operation(summary = "Delete One MobilieUrbain by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one mobilieUrbain"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/mobilie-urbain/{id}")
    public ResponseEntity<String> deleteOneMobilieUrbain(@PathVariable("mobilieUrbainId") Long mobilieUrbainId) {
        mobilieUrbainService.deleteOneMobilieUrbain(mobilieUrbainId);
        return ResponseEntity.ok()
                .body("Successfully delete");
    }
}
