package sonaged.collecte.master.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Data
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

    @JsonIgnore
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "geometry")
    //@JoinColumn(name = "geometryId")
    private List<Coordinate> coordinates;
}
