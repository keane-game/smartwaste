package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.model.AuthorityEntity;
import sonaged.collecte.master.repository.AuthorityRepository;
import sonaged.collecte.master.service.AuthorityService;

import java.util.List;

@RequiredArgsConstructor
@Service
class AuthorityServiceImpl implements AuthorityService {

    private final AuthorityRepository authorityRepository;

    @Override
    public AuthorityEntity readAuthority(Long authorityId) {
        return authorityRepository.findById(authorityId)
                .orElseThrow(() -> new ResourceNotFoundException (
                        "Authority with id [%s] not found ".formatted(authorityId)
                ));
    }

    @Override
    public List<AuthorityEntity> readAllAuthority() {
        var authorityList = authorityRepository.findAll();
        return authorityList.stream().toList();
    }

    @Override
    public AuthorityEntity createAuthority(AuthorityEntity authority) {
        return authorityRepository.save(authority);
    }

    @Override
    public AuthorityEntity updateAuthority(Long authorityId, AuthorityEntity authority) {
        var existedAuthority = authorityRepository.findById(authorityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authority with id [%s] not found ".formatted(authorityId)
                ));
        if(authority.getName() != null){
            existedAuthority.setName(authority.getName());
        }
        return authorityRepository.save(existedAuthority);
    }


    @Override
    public void deleteAuthority(Long authorityId) {
        var authority = authorityRepository.findById(authorityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authority with id [%s] not found ".formatted(authorityId)
                ));
        authorityRepository.delete(authority);
    }
}
