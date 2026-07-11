package sonaged.collecte.master.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sonaged.collecte.master.model.AuthorityEntity;
import sonaged.collecte.master.service.AuthorityService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/v1/authorities")
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
    public AuthorityEntity readAuthority(@PathVariable("authorityId") Long authorityId){
        return authorityService.readAuthority(authorityId);
    }

    @Operation(summary = "Get All Authority")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Authorities"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<AuthorityEntity> readAuthorities(){
        return authorityService.readAllAuthority();
    }

    @Operation(summary = "Create one Authority", description = "A role is a set of permissions that give access to product features")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Request sent by the client was syntactically incorrect"),
            @ApiResponse(responseCode = "500", description = "Internal server error during request processing")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public AuthorityEntity createAuthority(@RequestBody AuthorityEntity authority){
        return authorityService.createAuthority(authority);
    }

    @Operation(summary = "update One Authority by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Update one authority"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{authorityId}")
    public AuthorityEntity updateAuthority(@PathVariable("authorityId") Long authorityId, @RequestBody() AuthorityEntity authority) {
        return authorityService.updateAuthority(authorityId, authority);
    }

    @Operation(summary = "Delete One Authority by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete one authority"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{authorityId}")
    public String deleteAuthority(@PathVariable("authorityId") Long authorityId) {
        authorityService.deleteAuthority(authorityId);
        log.debug("deleteRole end ok - roQleId: {}", authorityId);
        return "Successfully delete";

    }

}
