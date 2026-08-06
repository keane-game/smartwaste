package sn.smartwaste.collect.identity.domain.model;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "USERS")
public class UserEntity extends  AbstractAuditingEntity<UUID> implements UserDetails {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "UserId", unique = true ,nullable = false)
    UUID userId;

    @Column(name = "UserLastname", nullable = false)
    String userLastname;

    @Column(name = "UserFirstname", nullable = false)
    String userFirstname;

    @Column(name = "UserEmail", nullable = false, unique = true)
    @Email
    String userEmail;


    @Column(name = "UserPassword", nullable = false)
    String userPassword;

    @Column(name = "UserCode")
    String userCode;

    @Column(name = "UserAddress")
    String userAddress;

    @Column(name = "UserPhone")
    String userPhone;

    @Column( columnDefinition="boolean default true")
    boolean activated = true;

    @JsonIgnore
    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(name = "authorityId", nullable = false)
    AuthorityEntity authority;


  /*  @ManyToMany
    @JoinTable(
            name = "ucg_user_authority",
            joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "userId") },
            inverseJoinColumns = { @JoinColumn(name = "authority_id", referencedColumnName = "authorityId") }
    )
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @BatchSize(size = 20)
    Set<Authority> authorities = new HashSet<>();*/


    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UserEntity user = (UserEntity) o;
        return getUserId() != null && Objects.equals(getUserId(), user.getUserId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }


    /**
     * Ce que ce compte porte auprès de Spring Security : son <b>rôle</b>, et les
     * <b>permissions</b> de ce rôle.
     *
     * <p><b>Ce que cela ferme.</b> Seul {@code ROLE_<nom>} était rendu : les permissions attachées
     * au rôle n'atteignaient jamais le contexte de sécurité. Les lignes de
     * {@code authoritypermission} étaient donc décoratives, et les seules règles qui les
     * mentionnaient — {@code AuthorityRules}, {@code UserRules}, retirées le 2026-08-06 — étaient
     * précisément celles que rien n'appliquait. Toute décision se prenait sur le <i>nom</i> du rôle,
     * si bien que changer qui peut faire quoi imposait de modifier du code, alors que le modèle
     * rôle→permissions existait déjà en base pour l'éviter.
     *
     * <p><b>Le préfixe distingue deux choses différentes.</b> {@code ROLE_} dit ce qu'on <i>est</i>
     * et répond à {@code hasRole} ; une permission nue dit ce qu'on <i>peut faire</i> et répond à
     * {@code hasAuthority}. Les confondre effacerait la distinction.
     *
     * <p><b>Une collection non chargée vaut « aucune permission », jamais une erreur.</b>
     * {@code AuthorityEntity.permissions} est en {@code LAZY} : hors session, y accéder lèverait
     * {@code LazyInitializationException} et casserait l'authentification entière — le même piège
     * que celui qui rendait {@code register} dépendant de l'open-session-in-view.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        var authorities = new java.util.ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.authority.getName()));

        try {
            var permissions = this.authority.getPermissions();
            if (permissions != null) {
                permissions.stream()
                        .filter(java.util.Objects::nonNull)
                        .map(p -> new SimpleGrantedAuthority(p.name()))
                        .forEach(authorities::add);
            }
        } catch (org.hibernate.LazyInitializationException e) {
            // Compte lu hors session : le rôle suffit à authentifier. Refuser ici priverait
            // l'appelant de toute identité pour une raison qui ne le concerne pas.
            LoggerFactory.getLogger(UserEntity.class).debug(
                    "Permissions non chargees pour le role {} : seul le role est expose",
                    this.authority.getName());
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return this.userPassword;
    }

    @Override
    public String getUsername() {
        return this.userEmail;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.activated;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.activated;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.activated;
    }

    @Override
    public boolean isEnabled() {
        return this.activated;
    }
}
