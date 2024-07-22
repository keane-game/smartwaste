package sonaged.collecte.master.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ModelAttribute;

import org.springframework.web.multipart.MultipartFile;
import sonaged.collecte.master.dto.Alert;
import sonaged.collecte.master.service.AlertService;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/alerts")
public class AlertController {
    private static final Logger log = LoggerFactory.getLogger (AlertController.class);
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
    @GetMapping("s")
    public ResponseEntity<List<Alert>> readAllAlerts(){
        return ResponseEntity
                .ok()
                .body(alertService.readAllAlert());
    }

    @Operation(summary = "Get All Alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Alerts"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<Alert> readAllAlert(@RequestParam("page") int page, @RequestParam("size") int size){
        Pageable pageable = PageRequest.of (page, size);
        return alertService.readAllAlert(pageable);
    }

    @Operation(summary = "Create one Alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one alert"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "s", consumes = {"multipart/form-data", "application/octet-stream", "application/json"})
    public Alert createAlertd(@RequestPart("alert") Alert alert, @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        return  alertService.createAlert(alert, file);
    }

    @Operation(summary = "Create one Alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one alert"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", consumes = {"multipart/form-data", "application/octet-stream", "application/json"})
    public Alert createAlert(@RequestParam("alert") String alert, @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        return  alertService.createAlert(alert, file);
    }

    @Operation(summary = "Create one Alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one alert"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/test", consumes = {"multipart/form-data", "application/octet-stream", "application/json"})
    public Alert createAlerts(@RequestBody Alert alert, @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        return  alertService.createAlert(alert, file);
    }

    @Operation(summary = "Create one Alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one alert"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/test1", consumes = {"multipart/form-data", "application/octet-stream", "application/json"})
    public Alert createAlerts1(@RequestBody Alert alert, @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        return  alertService.createAlert(alert, file);
    }

    @Operation(summary = "Create one Alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one alert"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/test2", consumes = {"multipart/form-data", "application/octet-stream", "application/json"})
    public Alert createAlerts2(@ModelAttribute Alert alert) throws IOException {
        return  alertService.createAlertFile(alert);
    }

    @Operation(summary = "update  Alert by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one alert"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "/{alertId}",  consumes = {"multipart/form-data", "application/octet-stream", "application/json"})
    public Alert updateAlert(@PathVariable("alertId") Long alertId, @RequestParam("alert") String alert, @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        return alertService.updateAlert(alertId, alert, file);
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