package sn.smartwaste.collect.identity.presentation.controller;

import java.util.UUID;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import sn.smartwaste.collect.identity.domain.model.AuthorityEntity;
import sn.smartwaste.collect.identity.application.service.AuthorityService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/v1/authorities")
// La gestion des roles est gouvernee par une PERMISSION, non par un nom de role.
//
// C'est le premier endroit ou le modele role->permissions, present en base depuis l'origine,
// decide reellement de quelque chose : les lignes de `authoritypermission` n'atteignaient pas le
// contexte de securite, et les seules regles qui les mentionnaient — AuthorityRules, UserRules —
// sont celles que rien n'applique.
//
// Comportement inchange : MANAGE_ROLE est seme sur ADMIN et SUPER_ADMIN. Ce qui change, c'est
// qu'un administrateur peut desormais retirer cette permission a un role sans toucher au code,
// et que la question « ADMIN doit-il pouvoir se hisser SUPER_ADMIN ? » (CLAUDE.md) devient une
// decision de donnees plutot qu'une livraison.
@PreAuthorize("hasAuthority('MANAGE_ROLE')")
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
    public AuthorityEntity readAuthority(@PathVariable("authorityId") UUID authorityId){
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
    public AuthorityEntity updateAuthority(@PathVariable("authorityId") UUID authorityId, @RequestBody() AuthorityEntity authority) {
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
    public String deleteAuthority(@PathVariable("authorityId") UUID authorityId) {
        authorityService.deleteAuthority(authorityId);
        log.debug("deleteRole end ok - roQleId: {}", authorityId);
        return "Successfully delete";

    }

}
