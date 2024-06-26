package sonaged.collecte.master.model;

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
public class UserEntity extends  AbstractAuditingEntity<Long> implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId", unique = true ,nullable = false)
    Long userId;

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

    @Column( columnDefinition="boolean default false")
    boolean activated = false;

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


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority ("ROLE_"+this.authority.getName()));
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
