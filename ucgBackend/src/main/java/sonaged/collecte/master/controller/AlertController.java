package sonaged.collecte.master.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.Alert;
import sonaged.collecte.master.service.AlertService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/alerts")
public class AlertController {
    private final AlertService alertService;

    @Operation(summary = "Get  Alert by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get  Alert"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{alertId}")
    public Alert readAlert(@PathVariable("alertId") Long alertId){
     return alertService.readAlert(alertId);

    }


    @Operation(summary = "Get All Alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Alerts"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ResponseEntity<List<Alert>> readAllAlert(){
        return ResponseEntity
                .ok()
                .body(alertService.readAllAlert());
    }

    @Operation(summary = "Create one Alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one alert"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Alert createAlert(@RequestBody Alert alert){
        return alertService.createAlert(alert);
    }

    @Operation(summary = "update  Alert by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one alert"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{alertId}")
    public Alert updateAlert(@PathVariable("alertId") Long alertId, @RequestBody() Alert alert) {
        alert.setAlertId (alertId);
        return alertService.updateAlert(alertId, alert);

    }

    @Operation(summary = "Delete  Alert by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one alert"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{alertId}")
    public String deleteAlert(@PathVariable("alertId") Long alertId) {
        alertService.deleteAlert(alertId);
        return "Successfully delete";
    }
}
