package ucg.collecte.master.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "UCG_GEOMETRY")
public class Geometry {

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
}
