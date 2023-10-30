package ucg.collecte.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ucg.collecte.master.dto.UserDto;
import ucg.collecte.master.service.UserService;

import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@RestController
@RequestMapping("api.ucg/v1")
public class UserController {


    private final UserService userService;

    @Operation(summary = "Get One User by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One User"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/user/{id}")
    public ResponseEntity<UserDto> getOneUser(@PathVariable("userId") Long userid){
        UserDto userDto = userService.getOneUser(userid);
        return ResponseEntity
                .ok()
                .body(userDto);
    }

    @Operation(summary = "Get All User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Users"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users/all")
    public ResponseEntity<List<UserDto>> getAllUser(){
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
    public ResponseEntity<UserDto> createOneUser(@RequestBody UserDto userDto){
        UserDto user = userService.createOneUser(userDto);
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
    @PutMapping("/update/user/{id}")
    public ResponseEntity<UserDto>  updateOneUser(@PathVariable("userId") Long userId, @RequestBody() UserDto userDto) {
        UserDto user = userService.updateOneUser(userId, userDto);
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
    @DeleteMapping("/delete/user/{id}")
    public ResponseEntity<String> deleteOneUser(@PathVariable("userId") Long userId) {
        userService.deleteOneUser(userId);
        return ResponseEntity.ok()
                .body("Successfully delete");
    }

}
