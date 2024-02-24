package sonaged.collecte.master.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SND_MOBLIERURBAIN")
public class MoblierUrbain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "moblierUrbainId")
    private  Long moblierUrbainId;

    @Column(name = "moblierUrbainName")
    private String moblierUrbainName;

    @Column(name = "moblierUrbainCode")
    private String moblierUrbainCode;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        MoblierUrbain that = (MoblierUrbain) o;
        return getMoblierUrbainId() != null && Objects.equals(getMoblierUrbainId(), that.getMoblierUrbainId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
