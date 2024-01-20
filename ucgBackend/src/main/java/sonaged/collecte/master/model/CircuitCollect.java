package sonaged.collecte.master.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SND_CIRCUITCOLLECT")
public class CircuitCollect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "circuitcollectId")
    private  Long circuitcollectId;

    @Column(name = "circuitcollectName")
    private String circuitcollectName;

    @Column(name = "circuitcollectCode")
    private String circuitcollectCode;

    @Column(name = "circuitcollectLength")
    private String circuitcollectLength;

    @Column(name = "circuitcollectFrequence")
    private String circuitcollectFrequence;

    @Column(name = "circuitcollectLatiPointA")
    private String circuitcollectLatiPointA;

    @Column(name = "circuitcollectLatiPointD")
    private String circuitcollectLatiPointD;

    @Column(name = "circuitcollectLongPointA")
    private String circuitcollectLongPointA;

    @Column(name = "circuitcollectLongPointD")
    private String circuitcollectLongPointD;

    @Column(name = "circuitcollectType")
    private String circuitcollectType;

    @Column(name = "circuitcollectCat")
    private String circuitcollectCat;

    @Column(name = "circuitcollectRotattion")
    private String circuitcollectRotation;

    @Column(name = "circuitcollectSection")
    private String circuitcollectSection;

    @Column(name = "circuitcollectSectection")
    private String circuitcollectSectection;


}
