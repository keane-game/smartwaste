package sonaged.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.dto.User;
import sonaged.collecte.master.dto.UserResponse;
import sonaged.collecte.master.service.UserService;

import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/users")
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
    public User readUser(@PathVariable("userId") Long userId){
        return userService.readUser (userId);
    }

    @Operation(summary = "Get All User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Users"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<User> readAllUser(){
        return userService.readAllUser ();
    }

    @Operation(summary = "Create one User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one user"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public User createUser(@RequestBody User user){
        return userService.createUser (user);
    }

    @Operation(summary = "update One User by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one user"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{userId}")
    public User updateUser(@PathVariable("userId") Long userId, @RequestBody() User user) {
        return userService.updateUser (userId, user);
    }

    @Operation(summary = "Delete One User by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one user"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteUser (userId);
        return "Successfully delete";
    }

}
