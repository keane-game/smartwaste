package sn.smartwaste.collect.identity.application.service.impl;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.identity.domain.model.AuthorityEntity;
import sn.smartwaste.collect.identity.domain.repository.AuthorityRepository;
import sn.smartwaste.collect.identity.application.service.AuthorityService;

import java.util.List;

@RequiredArgsConstructor
@Service
class AuthorityServiceImpl implements AuthorityService {

    private final AuthorityRepository authorityRepository;

    @Override
    public AuthorityEntity readAuthority(UUID authorityId) {
        return authorityRepository.findById(authorityId)
                .orElseThrow(() -> new ResourceNotFoundException (
                        "Authority with id [%s] not found ".formatted(authorityId)
                ));
    }

    @Override
    public List<AuthorityEntity> readAllAuthority() {
        var authorityList = authorityRepository.findByDeletionStatus(sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE);
        return authorityList.stream().toList();
    }

    @Override
    public AuthorityEntity createAuthority(AuthorityEntity authority) {
        return authorityRepository.save(authority);
    }

    @Override
    public AuthorityEntity updateAuthority(UUID authorityId, AuthorityEntity authority) {
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
    public void deleteAuthority(UUID authorityId) {
        var authority = authorityRepository.findById(authorityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authority with id [%s] not found ".formatted(authorityId)
                ));
        authority.markForDeletion(java.time.LocalDateTime.now()); authorityRepository.save(authority); // soft-delete (rétention + purge planifiée)
    }
}
