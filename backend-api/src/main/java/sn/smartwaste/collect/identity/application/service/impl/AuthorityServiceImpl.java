package sn.smartwaste.collect.identity.application.service.impl;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.identity.application.dto.Authority;
import sn.smartwaste.collect.identity.application.mapper.AuthorityMapper;
import sn.smartwaste.collect.identity.domain.repository.AuthorityRepository;
import sn.smartwaste.collect.identity.application.service.AuthorityService;

import java.util.List;

// P1-2/audit : AuthorityController exposait l'entité JPA `AuthorityEntity` directement (aucun
// DTO) sur un endpoint de gestion des rôles — même défaut que Region/Department/Quartier déjà
// corrigé, ici sans DTO intermédiaire du tout plutôt qu'un simple champ qui fuit.
@RequiredArgsConstructor
@Service
class AuthorityServiceImpl implements AuthorityService {

    private final AuthorityRepository authorityRepository;

    @Override
    public Authority readAuthority(UUID authorityId) {
        var authority = authorityRepository.findById(authorityId)
                .orElseThrow(() -> new ResourceNotFoundException (
                        "Authority with id [%s] not found ".formatted(authorityId)
                ));
        return AuthorityMapper.AMP.asDto(authority);
    }

    @Override
    public List<Authority> readAllAuthority() {
        var authorityList = authorityRepository.findByDeletionStatus(sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE);
        return AuthorityMapper.AMP.asListDto(authorityList);
    }

    @Override
    public Authority createAuthority(Authority authority) {
        var saved = authorityRepository.save(AuthorityMapper.AMP.asModel(authority));
        return AuthorityMapper.AMP.asDto(saved);
    }

    @Override
    public Authority updateAuthority(UUID authorityId, Authority authority) {
        var existedAuthority = authorityRepository.findById(authorityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authority with id [%s] not found ".formatted(authorityId)
                ));
        if(authority.getName() != null){
            existedAuthority.setName(authority.getName());
        }
        // Corrige un defaut releve par audit (2026-08-09, ADR-0021) : les permissions envoyees
        // dans le corps de la requete etaient ignorees en silence, seul `name` etait applique — un
        // administrateur modifiant les droits d'un role existant n'avait donc aucun effet.
        if (authority.getPermissions() != null) {
            existedAuthority.setPermissions(authority.getPermissions());
        }
        return AuthorityMapper.AMP.asDto(authorityRepository.save(existedAuthority));
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
