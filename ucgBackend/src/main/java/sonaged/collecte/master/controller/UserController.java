package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.UserDto;
import sonaged.collecte.master.dto.UserResponse;
import sonaged.collecte.master.service.UserService;

import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("api/user")
public class UserController {


    private final UserService userService;

    @Operation(summary = "Get One User by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One User"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{userId}")
    public UserResponse getOneUser(@PathVariable("userId") Long userId){
        return userService.getOneUser(userId);
    }

    @Operation(summary = "Get All User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Users"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<UserResponse> getAllUser(){
        return userService.getAllUser();
    }

    @Operation(summary = "Create one User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one user"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public UserResponse createOneUser(@RequestBody UserDto userDto){
        return userService.createOneUser(userDto);
    }

    @Operation(summary = "update One User by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one user"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{userId}")
    public UserResponse updateOneUser(@PathVariable("userId") Long userId, @RequestBody() UserResponse userResponse) {
        return userService.updateOneUser(userId, userResponse);
    }

    @Operation(summary = "Delete One User by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one user"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{userId}")
    public String deleteOneUser(@PathVariable("userId") Long userId) {
        userService.deleteOneUser(userId);
        return "Successfully delete";
    }

}
