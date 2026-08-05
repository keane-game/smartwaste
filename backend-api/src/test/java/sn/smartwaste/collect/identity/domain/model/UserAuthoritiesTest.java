package sn.smartwaste.collect.identity.domain.model;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ce qu'un compte porte réellement auprès de Spring Security.
 *
 * <p><b>Le défaut fermé ici.</b> {@code getAuthorities()} ne rendait que
 * {@code ROLE_<nom du rôle>} : les <b>permissions</b> attachées au rôle n'atteignaient jamais le
 * contexte de sécurité. Les neuf lignes de {@code authoritypermission} en base étaient donc
 * décoratives, et les seules règles qui les mentionnent
 * ({@code AuthorityRules}, {@code UserRules}) sont précisément celles que rien n'applique.
 *
 * <p>Toute décision d'autorisation se prenait donc sur le <i>nom</i> du rôle. Conséquence
 * pratique : changer qui peut faire quoi imposait de modifier du code et de redéployer, alors que
 * le modèle rôle→permissions existait déjà en base pour l'éviter.
 *
 * <p><b>Le piège à ne pas rouvrir.</b> {@code AuthorityEntity.permissions} est en
 * {@code FetchType.LAZY}. Les exposer sans précaution ferait lever
 * {@code LazyInitializationException} dès que le compte est lu hors session — c'est exactement ce
 * qui rendait {@code AuthServiceImpl.register} intestable et dépendant de l'open-session-in-view.
 * Une collection non initialisée doit donc être traitée comme « aucune permission », pas comme une
 * erreur.
 */
class UserAuthoritiesTest {

    private UserEntity userWith(AuthorityEntity authority) {
        var user = new UserEntity();
        user.setAuthority(authority);
        return user;
    }

    private AuthorityEntity authority(String name, Permission... permissions) {
        var a = new AuthorityEntity();
        a.setName(name);
        a.setPermissions(List.of(permissions));
        return a;
    }

    @Test
    @DisplayName("le role reste expose sous la forme attendue par hasRole")
    void roleIsStillExposed() {
        // `hasAnyRole("ADMIN")` compare a `ROLE_ADMIN` : toute la surface d'administration en depend.
        var authorities = userWith(authority("ADMIN")).getAuthorities();

        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN");
    }

    @Test
    @DisplayName("les permissions du role deviennent des autorites")
    void permissionsBecomeAuthorities() {
        var authorities = userWith(authority("ADMIN",
                Permission.MANAGE_ROLE, Permission.CREATE_USER)).getAuthorities();

        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN", "MANAGE_ROLE", "CREATE_USER");
    }

    @Test
    @DisplayName("une permission n'est pas prefixee ROLE_")
    void permissionsAreNotRoles() {
        // Sinon `hasRole('MANAGE_ROLE')` passerait, et la distinction entre ce qu'on EST et ce
        // qu'on PEUT FAIRE disparaitrait.
        var authorities = userWith(authority("ADMIN", Permission.MANAGE_ROLE)).getAuthorities();

        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .doesNotContain("ROLE_MANAGE_ROLE");
    }

    @Test
    @DisplayName("un role sans permission reste utilisable")
    void roleWithoutPermissionStillWorks() {
        var authorities = userWith(authority("USER")).getAuthorities();

        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("une collection de permissions non initialisee ne fait pas echouer l'authentification")
    void uninitializedPermissionsDoNotBreakAuthentication() {
        // Le piege : `permissions` est LAZY. Hors session, la collection peut etre nulle ou lever.
        // Traiter ce cas comme « aucune permission » evite qu'une lecture de compte hors
        // transaction — filtre JWT, tache planifiee, test — ne casse toute l'authentification.
        var sansPermissions = new AuthorityEntity();
        sansPermissions.setName("ADMIN");
        sansPermissions.setPermissions(null);

        var authorities = userWith(sansPermissions).getAuthorities();

        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }
}
