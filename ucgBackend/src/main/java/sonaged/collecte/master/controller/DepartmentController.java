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
@RequestMapping("api/department")
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
    public DepartmentDto getOneDepartment(@PathVariable("departmentId") Long departmentid){
        return departmentService.getOneDepartment(departmentid);
    }

    @Operation(summary = "Get All Department")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Departments"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<DepartmentDto> getAllDepartment(){
        return departmentService.getAllDepartment();
    }

    @Operation(summary = "Create one Department")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one department"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public DepartmentDto createOneDepartment(@RequestBody DepartmentDto departmentDto){
        return departmentService.createOneDepartment(departmentDto);
    }

    @Operation(summary = "update One Department by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one department"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{departmentId}")
    public DepartmentDto updateOneDepartment(@PathVariable("departmentId") Long departmentId, @RequestBody() DepartmentDto departmentDto) {
        return departmentService.updateOneDepartment(departmentId, departmentDto);
    }

    @Operation(summary = "Delete One Department by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one department"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{departmentId}")
    public String deleteOneDepartment(@PathVariable("departmentId") Long departmentId) {
        departmentService.deleteOneDepartment(departmentId);
        return "Successfully delete";
    }
}
