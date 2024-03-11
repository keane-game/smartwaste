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
@RequestMapping("api")
public class UserController {


    private final UserService userService;

    @Operation(summary = "Get One User by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One User"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserResponse> getOneUser(@PathVariable("userId") Long userId){
        UserResponse userResponse = userService.getOneUser(userId);
        return ResponseEntity
                .ok()
                .body(userResponse);
    }

    @Operation(summary = "Get All User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Users"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/users/all")
    public ResponseEntity<List<UserResponse>> getAllUser(){
        return ResponseEntity
                .ok()
                .body(userService.getAllUser());
    }

    @Operation(summary = "Create one User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one user"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/create/user")
    public ResponseEntity<UserResponse> createOneUser(@RequestBody UserDto userDto){
        UserResponse user = userService.createOneUser(userDto);
        return ResponseEntity.ok()
                .body(user);
    }

    @Operation(summary = "update One User by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one user"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/update/user/{userId}")
    public ResponseEntity<UserResponse>  updateOneUser(@PathVariable("userId") Long userId, @RequestBody() UserResponse userResponse) {
        UserResponse user = userService.updateOneUser(userId, userResponse);
        return ResponseEntity.ok()
                .body(user);
    }

    @Operation(summary = "Delete One User by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one user"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/user/{userId}")
    public ResponseEntity<String> deleteOneUser(@PathVariable("userId") Long userId) {
        userService.deleteOneUser(userId);
        return ResponseEntity.ok()
                .body("Successfully delete");
    }

}
