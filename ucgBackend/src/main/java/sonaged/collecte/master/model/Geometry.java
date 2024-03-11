package sonaged.collecte.master.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SND_GEOMETRY")
@ToString
public class Geometry  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "geometryId")
    private  Long geometryId;

    @Column(name = "geometryType")
    private String geometryType;

    @Column(name = "spatialReference")
    private String spatialReference;

    @Column(name = "geometryRing")
    private String geometryRing;

    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "geometryId")
    private List<Coordinate> coordinates;



}
