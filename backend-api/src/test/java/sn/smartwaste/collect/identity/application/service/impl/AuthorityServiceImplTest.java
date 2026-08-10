package sn.smartwaste.collect.identity.application.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.identity.domain.model.AuthorityEntity;
import sn.smartwaste.collect.identity.domain.model.Permission;
import sn.smartwaste.collect.identity.domain.repository.AuthorityRepository;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.shared.domain.model.DeletionStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests du service des rôles.
 *
 * <p>Les cinq méthodes de cette classe étaient jusqu'ici <b>vides</b> : la suite affichait
 * « Tests run: 5, Failures: 0 » sans exécuter la moindre assertion (constat P1-7b). Elles sont
 * réécrites ici, à l'occasion de la migration du contexte, pour couvrir ce que le service fait
 * réellement — y compris ses deux comportements contre-intuitifs : la lecture ne renvoie que les
 * rôles actifs, et la suppression est logique.
 */
@ExtendWith(MockitoExtension.class)
class AuthorityServiceImplTest {

    @Mock
    private AuthorityRepository authorityRepository;

    @InjectMocks
    private AuthorityServiceImpl authorityService;

    private static AuthorityEntity authority(UUID id, String name) {
        AuthorityEntity authority = new AuthorityEntity();
        authority.setAuthorityId(id);
        authority.setName(name);
        return authority;
    }

    @Test
    @DisplayName("readAuthority renvoie le rôle demandé")
    void readAuthority_returnsEntity() {
        UUID id = UUID.randomUUID();
        when(authorityRepository.findById(id)).thenReturn(Optional.of(authority(id, "ADMIN")));

        assertThat(authorityService.readAuthority(id).getName()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("readAuthority sur un identifiant inconnu lève ResourceNotFoundException")
    void readAuthority_unknownIdThrows() {
        UUID id = UUID.randomUUID();
        when(authorityRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorityService.readAuthority(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("readAllAuthority ne remonte que les rôles ACTIVE, jamais ceux en attente de purge")
    void readAllAuthority_filtersOnActiveStatus() {
        when(authorityRepository.findByDeletionStatus(DeletionStatus.ACTIVE))
                .thenReturn(List.of(authority(UUID.randomUUID(), "USER")));

        assertThat(authorityService.readAllAuthority()).hasSize(1);
        // findAll() renverrait aussi les rôles supprimés logiquement : la distinction est le
        // cœur du soft-delete, elle doit rester vérifiée.
        verify(authorityRepository, never()).findAll();
    }

    @Test
    @DisplayName("createAuthority persiste le rôle tel quel")
    void createAuthority_savesEntity() {
        AuthorityEntity toCreate = authority(null, "SUPPORT");
        when(authorityRepository.save(toCreate)).thenReturn(toCreate);

        assertThat(authorityService.createAuthority(toCreate)).isSameAs(toCreate);
    }

    @Test
    @DisplayName("updateAuthority applique le nom sur l'entité existante et ignore un nom absent")
    void updateAuthority_patchesNameOnly() {
        UUID id = UUID.randomUUID();
        AuthorityEntity existing = authority(id, "ADMIN");
        when(authorityRepository.findById(id)).thenReturn(Optional.of(existing));
        when(authorityRepository.save(any(AuthorityEntity.class))).thenAnswer(i -> i.getArgument(0));

        AuthorityEntity renamed = authorityService.updateAuthority(id, authority(null, "ADMIN_V2"));
        assertThat(renamed.getName()).isEqualTo("ADMIN_V2");

        // Un champ nul est une absence de modification, pas un effacement.
        AuthorityEntity untouched = authorityService.updateAuthority(id, authority(null, null));
        assertThat(untouched.getName()).isEqualTo("ADMIN_V2");
    }

    @Test
    @DisplayName("updateAuthority applique aussi les permissions envoyees, plus seulement le nom")
    void updateAuthority_patchesPermissions() {
        // Defaut releve par audit (2026-08-09, ADR-0021) : seul `name` etait recopie, les
        // permissions du corps de la requete etaient ignorees en silence — un administrateur
        // modifiant les droits d'un role existant n'avait donc aucun effet observable.
        UUID id = UUID.randomUUID();
        AuthorityEntity existing = authority(id, "AGENT");
        existing.setPermissions(List.of(Permission.VIEW_COLLECTION_ROUTE));
        when(authorityRepository.findById(id)).thenReturn(Optional.of(existing));
        when(authorityRepository.save(any(AuthorityEntity.class))).thenAnswer(i -> i.getArgument(0));

        AuthorityEntity request = authority(null, null);
        request.setPermissions(List.of(Permission.VIEW_COLLECTION_ROUTE, Permission.DECLARE_COLLECTION));

        AuthorityEntity updated = authorityService.updateAuthority(id, request);

        assertThat(updated.getPermissions())
                .containsExactlyInAnyOrder(Permission.VIEW_COLLECTION_ROUTE, Permission.DECLARE_COLLECTION);
    }

    @Test
    @DisplayName("updateAuthority sans permissions dans la requete laisse les permissions existantes intactes")
    void updateAuthority_missingPermissionsLeavesExistingUntouched() {
        UUID id = UUID.randomUUID();
        AuthorityEntity existing = authority(id, "AGENT");
        existing.setPermissions(List.of(Permission.VIEW_COLLECTION_ROUTE));
        when(authorityRepository.findById(id)).thenReturn(Optional.of(existing));
        when(authorityRepository.save(any(AuthorityEntity.class))).thenAnswer(i -> i.getArgument(0));

        AuthorityEntity updated = authorityService.updateAuthority(id, authority(null, "AGENT_V2"));

        assertThat(updated.getPermissions()).containsExactly(Permission.VIEW_COLLECTION_ROUTE);
    }

    @Test
    @DisplayName("deleteAuthority marque le rôle en PENDING_DELETION au lieu de le supprimer")
    void deleteAuthority_isSoftDelete() {
        UUID id = UUID.randomUUID();
        when(authorityRepository.findById(id)).thenReturn(Optional.of(authority(id, "ADMIN")));

        authorityService.deleteAuthority(id);

        ArgumentCaptor<AuthorityEntity> saved = ArgumentCaptor.forClass(AuthorityEntity.class);
        verify(authorityRepository).save(saved.capture());
        assertThat(saved.getValue().isPendingDeletion()).isTrue();
        assertThat(saved.getValue().getDeletionRequestedAt()).isNotNull();
        // Une suppression physique rendrait la corbeille et la restauration inopérantes.
        verify(authorityRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteAuthority sur un identifiant inconnu lève ResourceNotFoundException")
    void deleteAuthority_unknownIdThrows() {
        UUID id = UUID.randomUUID();
        when(authorityRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorityService.deleteAuthority(id))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(authorityRepository, never()).save(any());
    }
}
