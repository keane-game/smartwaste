package sn.smartwaste.collect.identity.application.service;

import java.util.UUID;

import sn.smartwaste.collect.identity.application.dto.Authority;

import java.util.List;

public interface AuthorityService {

    Authority readAuthority(UUID authorityId);

    List<Authority> readAllAuthority();

    Authority createAuthority(Authority authority);

    Authority updateAuthority (UUID authorityId, Authority authority);
    void deleteAuthority(UUID authorityId);
}
