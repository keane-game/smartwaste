package sonaged.collecte.master.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.model.Authority;
import sonaged.collecte.master.service.AuthorityService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/authority")
public class AuthorityController {

    private final AuthorityService authorityService;

    @Operation(summary = "Get One Authority by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get One Authority"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{authorityId}")
    public ResponseEntity<Authority> getOneAuthority(@PathVariable("authorityId") Long authorityId){
        Authority authority = authorityService.getOneAuthority(authorityId);
        return ResponseEntity
                .ok()
                .body(authority);
    }

    @Operation(summary = "Get All Authority")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Authoritys"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ResponseEntity<List<Authority>> getAllAuthority(){
        return ResponseEntity
                .ok()
                .body(authorityService.getAllAuthority());
    }

    @Operation(summary = "Create one Authority", description = "A role is a set of permissions that give access to product features")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<Authority> createOneAuthority(@RequestBody Authority authority){
        Authority createAuthority = authorityService.createOneAuthority(authority);
        return ResponseEntity.ok()
                .body(createAuthority);
    }

    @Operation(summary = "update One Authority by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one authority"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{authorityId}")
    public ResponseEntity<Authority>  updateOneAuthority(@PathVariable("authorityId") Long authorityId, @RequestBody() Authority authority) {
        Authority updatedAuthority = authorityService.updateOneAuthority(authorityId, authority);
        return ResponseEntity.ok()
                .body(updatedAuthority);
    }

    @Operation(summary = "Delete One Authority by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one authority"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{authorityId}")
    public ResponseEntity<String> deleteOneAuthority(@PathVariable("authorityId") Long authorityId) {
        authorityService.deleteOneAuthority(authorityId);
        log.debug("deleteRole end ok - roQleId: {}", authorityId);
        return ResponseEntity.ok()
                .body("Successfully delete");

    }

}
