package sn.smartwaste.collect.identity.presentation.controller;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.smartwaste.collect.identity.application.dto.User;
import sn.smartwaste.collect.identity.application.service.UserService;

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
    @GetMapping("/{id}")
    public User readUser(@PathVariable("id") UUID id){
        return userService.readUser (id);
    }

    @Operation(summary = "Get All User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Users"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("s")
    public List<User> readAllUser(){
        return userService.readAllUser ();
    }


    @Operation(summary = "Read user by pagination with size, optionally filtered by search term",
            description = "Read users. `q` matches email, first name or last name (case-insensitive, partial).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request - request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "404", description = "Resource access does not exist"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")

    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<User> readAllUser(@RequestParam("page") int page, @RequestParam("size") int size,
                                   @RequestParam(value = "q", required = false) String q) {
        Pageable pageable = PageRequest.of (page, size);
        return userService.searchUser(q, pageable);
    }
    @Operation(summary = "Create one User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create one user"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
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
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}")
    public User updateUser(@PathVariable("id") UUID id, @RequestBody() User user) {
        return userService.updateUser (id, user);
    }

    @Operation(summary = "Delete One User by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one user"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable("id") UUID id) {
        userService.deleteUser (id);
        return "Successfully delete";
    }

    @Operation(summary = "Reactiver un compte utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compte reactive"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{id}/activate")
    public User activateUser(@PathVariable("id") UUID id) {
        return userService.activateUser(id);
    }

    @Operation(summary = "Desactiver un compte utilisateur",
            description = "Ferme immediatement toutes les sessions ouvertes du compte")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compte desactive"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{id}/deactivate")
    public User deactivateUser(@PathVariable("id") UUID id) {
        return userService.deactivateUser(id);
    }

}
