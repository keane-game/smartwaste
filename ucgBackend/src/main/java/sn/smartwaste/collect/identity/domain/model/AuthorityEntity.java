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
import jakarta.persistence.EnumType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;
import sn.smartwaste.collect.identity.domain.model.Permission;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

//@TableGenerator(name = "RoleGen", table = "JPA_SEQUENCES", pkColumnName = "SEQ_KEY", valueColumnName = "SEQ_VALUE", pkColumnValue = "authorityId", allocationSize = 1)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "AUTHORITY")
public class AuthorityEntity extends AbstractAuditingEntity<UUID> {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "authorityId")
    UUID authorityId;

    @NotNull
    @Size(max = 50)
    @Column(length = 50, name = "Name")
    String name;

    @Column(name = "Description")
    String description;

    @JsonIgnore
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "authority")
    @ToString.Exclude
    List<UserEntity> users;

    @ElementCollection(targetClass = Permission.class, fetch = FetchType.LAZY)
    @JoinTable(name = "authorityPermission", joinColumns = @JoinColumn(name = "authorityId"))
    @Column(name = "permission", nullable = false)
    @Enumerated(EnumType.STRING)
    Collection<Permission> permissions;


    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        AuthorityEntity authority = (AuthorityEntity) o;
        return getAuthorityId() != null && Objects.equals(getAuthorityId(), authority.getAuthorityId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
