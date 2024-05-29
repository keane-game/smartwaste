package sonaged.collecte.master.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.AlertDto;
import sonaged.collecte.master.service.AlertService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api")
public class AlertController {
    private final AlertService alertService;

    @Operation(summary = "Get One Alert by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Alert"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/alert/{alertId}")
    public ResponseEntity<AlertDto> getOneAlert(@PathVariable("alertId") Long alertId){
        AlertDto alertDto = alertService.getOneAlert(alertId);
        return ResponseEntity
                .ok()
                .body(alertDto);
    }


    @Operation(summary = "Get All Alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Alerts"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/alerts/all")
    public ResponseEntity<List<AlertDto>> getAllAlert(){
        return ResponseEntity
                .ok()
                .body(alertService.getAllAlert());
    }

    @Operation(summary = "Create one Alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one alert"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/create/alert")
    public ResponseEntity<Alert> createOneAlert(@RequestBody Alert alert){
        AlertDto alert = alertService.createOneAlert(alert);
        return ResponseEntity.ok()
                .body(alert);
    }

    @Operation(summary = "update One Alert by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one alert"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/update/alert/{alertId}")
    public ResponseEntity<AlertDto>  updateOneAlert(@PathVariable("alertId") Long alertId, @RequestBody() AlertDto alertDto) {
        AlertDto alert = alertService.updateOneAlert(alertId, alertDto);
        return ResponseEntity.ok()
                .body(alert);
    }

    @Operation(summary = "Delete One Alert by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one alert"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/alert/{alertId}")
    public ResponseEntity<String> deleteOneAlert(@PathVariable("alertId") Long alertId) {
        alertService.deleteOneAlert(alertId);
        return ResponseEntity.ok()
                .body("Successfully delete");
    }
}
