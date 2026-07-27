package sn.smartwaste.collect.territory.presentation.controller;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.smartwaste.collect.territory.application.dto.Department;
import sn.smartwaste.collect.territory.application.service.DepartmentService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    @Operation(summary = "Get One Department by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Department"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{departmentId}")
    public Department readDepartment(@PathVariable("departmentId") UUID departmentid){
        return departmentService.readDepartment(departmentid);
    }

    @Operation(summary = "Get All Department")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Departments"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<Department> readAllDepartment(){
        return departmentService.readAllDepartment();
    }

    @Operation(summary = "Create one Department")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one department"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public Department createDepartment(@RequestBody Department department){
        return departmentService.createDepartment (department);
    }

    @Operation(summary = "update One Department by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one department"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{departmentId}")
    public Department updateOneDepartment(@PathVariable("departmentId") UUID departmentId, @RequestBody() Department department) {
        return departmentService.updateDepartment (departmentId, department);
    }

    @Operation(summary = "Delete One Department by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one department"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{departmentId}")
    public String deleteDepartment(@PathVariable("departmentId") UUID departmentId) {
        departmentService.deleteDepartment (departmentId);
        return "Successfully delete";
    }
}
