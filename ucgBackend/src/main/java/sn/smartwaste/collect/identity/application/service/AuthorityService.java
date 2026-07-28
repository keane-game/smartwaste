package sn.smartwaste.collect.identity.application.service;

import java.util.UUID;

import sn.smartwaste.collect.identity.domain.model.AuthorityEntity;

import java.util.List;

public interface AuthorityService {

    AuthorityEntity readAuthority(UUID authorityId);

    List<AuthorityEntity> readAllAuthority();

    AuthorityEntity createAuthority(AuthorityEntity authority);

    AuthorityEntity updateAuthority (UUID authorityId, AuthorityEntity authority);
    void deleteAuthority(UUID authorityId);
}
