package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.DepartmentDto;
import sonaged.collecte.master.service.DepartmentService;

import java.util.List;

@RestController
@AllArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;

    @Operation(summary = "Get One Department by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Department"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/department/{id}")
    public ResponseEntity<DepartmentDto> getOneDepartment(@PathVariable("departmentId") Long departmentid){
        DepartmentDto departmentDto = departmentService.getOneDepartment(departmentid);
        return ResponseEntity
                .ok()
                .body(departmentDto);
    }

    @Operation(summary = "Get All Department")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Departments"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/departments/all")
    public ResponseEntity<List<DepartmentDto>> getAllDepartment(){
        return ResponseEntity
                .ok()
                .body(departmentService.getAllDepartment());
    }

    @Operation(summary = "Create one Department")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one department"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/create/department")
    public ResponseEntity<DepartmentDto> createOneDepartment(@RequestBody DepartmentDto departmentDto){
        DepartmentDto department = departmentService.createOneDepartment(departmentDto);
        return ResponseEntity.ok()
                .body(department);
    }

    @Operation(summary = "update One Department by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one department"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/update/department/{id}")
    public ResponseEntity<DepartmentDto>  updateOneDepartment(@PathVariable("departmentId") Long departmentId, @RequestBody() DepartmentDto departmentDto) {
        DepartmentDto department = departmentService.updateOneDepartment(departmentId, departmentDto);
        return ResponseEntity.ok()
                .body(department);
    }

    @Operation(summary = "Delete One Department by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one department"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/department/{id}")
    public ResponseEntity<String> deleteOneDepartment(@PathVariable("departmentId") Long departmentId) {
        departmentService.deleteOneDepartment(departmentId);
        return ResponseEntity.ok()
                .body("Successfully delete");
    }
}
