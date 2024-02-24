package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.model.Authority;
import sonaged.collecte.master.repository.AuthorityRepository;
import sonaged.collecte.master.service.AuthorityService;

import java.util.List;

@RequiredArgsConstructor
@Service
class AuthorityServiceImpl implements AuthorityService {

    private final AuthorityRepository authorityRepository;
    /**
     * @param authorityId
     * @return
     */
    @Override
    public Authority getOneAuthority(Long authorityId) {
        Authority authority = authorityRepository.findById(authorityId)
                .orElseThrow(() -> new ResourceNotFoundException (
                        "User with id [%s] not found ".formatted(authorityId)
                ));
        return authority;
    }

    /**
     * @return
     */
    @Override
    public List<Authority> getAllAuthority() {
        List<Authority> authorityList = authorityRepository.findAll();
        return authorityList.stream().toList();
    }

    /**
     * @param authority
     * @return
     */
    @Override
    public Authority createOneAuthority(Authority authority) {
        return authorityRepository.save(authority);
    }

    /**
     * @param authorityId
     * @param authority
     * @return
     */
    @Override
    public Authority updateOneAuthority(Long authorityId, Authority authority) {
        Authority existedAuthority = authorityRepository.findById(authorityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authority with id [%s] not found ".formatted(authorityId)
                ));
        if(authority.getAuthorityName() != null){
            existedAuthority.setAuthorityName(authority.getAuthorityName());
        }
        return authorityRepository.save(existedAuthority);
    }

    /**
     * @param authorityId
     */
    @Override
    public void deleteOneAuthority(Long authorityId) {
        Authority authority = authorityRepository.findById(authorityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authority with id [%s] not found ".formatted(authorityId)
                ));
        authorityRepository.delete(authority);
    }
}
