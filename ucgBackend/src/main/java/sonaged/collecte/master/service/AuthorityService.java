package sonaged.collecte.master.service;

import sonaged.collecte.master.model.Authority;

import java.util.List;

public interface AuthorityService {

    Authority getOneAuthority(Long authorityId);

    List<Authority> getAllAuthority();

    Authority createOneAuthority(Authority authority);

    Authority updateOneAuthority (Long authorityId, Authority authority);
    void deleteOneAuthority(Long authorityId);
}
