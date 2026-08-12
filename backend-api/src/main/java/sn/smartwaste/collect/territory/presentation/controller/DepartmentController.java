package sn.smartwaste.collect.territory.presentation.controller;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    @GetMapping("s")
    public List<Department> readAllDepartment(){
        return departmentService.readAllDepartment();
    }

    /**
     * Corrige une incohérence relevée par audit (2026-08-10, `docs/FRONTEND_API_MAPPING.md`) :
     * cette ressource n'avait qu'une liste plate, contrairement à Commune/Quartier/Depotoir/User/
     * Alert. Même patron que ces cinq : chemin nu paginé, `/s` pour la liste complète. Le service
     * portait déjà cette méthode, jamais exposée jusqu'ici.
     */
    @Operation(summary = "Read Department by pagination with size", description = "Read Departments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<Department> readAllDepartment(@RequestParam("page") int page, @RequestParam("size") int size){
        Pageable pageable = PageRequest.of(page, size);
        return departmentService.readAllDepartment(pageable);
    }

    @Operation(summary = "Create one Department")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one department"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
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
    @ResponseStatus(HttpStatus.OK)
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
