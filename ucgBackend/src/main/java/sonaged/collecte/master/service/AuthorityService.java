package sonaged.collecte.master.service;

import sonaged.collecte.master.model.AuthorityEntity;

import java.util.List;

public interface AuthorityService {

    AuthorityEntity readAuthority(Long authorityId);

    List<AuthorityEntity> readAllAuthority();

    AuthorityEntity createAuthority(AuthorityEntity authority);

    AuthorityEntity updateAuthority (Long authorityId, AuthorityEntity authority);
    void deleteAuthority(Long authorityId);
}
