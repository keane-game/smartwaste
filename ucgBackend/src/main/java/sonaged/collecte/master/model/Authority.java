package sonaged.collecte.master.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.proxy.HibernateProxy;
import sonaged.collecte.master.enums.Permission;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

//@TableGenerator(name = "RoleGen", table = "JPA_SEQUENCES", pkColumnName = "SEQ_KEY", valueColumnName = "SEQ_VALUE", pkColumnValue = "authorityId", allocationSize = 1)
//@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "SND_AUTHORITY")
public class Authority extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "authorityId")
    private  Long authorityId;

    @NotNull
    @Size(max = 50)
    @Column(length = 50, name = "authorityName")
    private String authorityName;


    @Column(name = "authorityRealm", nullable = false)
    String authorityRealm;

    @Column(name = "authorityDescription")
    String authorityDescription;

    @JsonIgnore
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "authority")
    @ToString.Exclude
    private List<User> users;

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
        Authority authority = (Authority) o;
        return getAuthorityId() != null && Objects.equals(getAuthorityId(), authority.getAuthorityId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
