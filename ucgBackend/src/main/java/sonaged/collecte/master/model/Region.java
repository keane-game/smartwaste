package sonaged.collecte.master.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SND_REGION")
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "regionId")
    private  Long regionId;

    @Column(name = "regionName")
    private String regionName;

    @Column(name = "regionCode")
    private String regionCode;

    @OneToOne(fetch = FetchType.LAZY,  cascade = CascadeType.ALL)
    @JoinColumn(name = "geometryId", nullable = false)
    @JsonIgnore
    Geometry geometry;

    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "region")
    @JsonIgnore
    private List<Department> departments;

}
