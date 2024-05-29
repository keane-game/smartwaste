package sonaged.collecte.master.service;

import sonaged.collecte.master.model.AuthorityEntity;

import java.util.List;

public interface AuthorityService {

    AuthorityEntity getOneAuthority(Long authorityId);

    List<AuthorityEntity> getAllAuthority();

    AuthorityEntity createOneAuthority(AuthorityEntity authority);

    AuthorityEntity updateOneAuthority (Long authorityId, AuthorityEntity authority);
    void deleteOneAuthority(Long authorityId);
}
