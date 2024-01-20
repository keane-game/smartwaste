package sonaged.collecte.master.dto;


import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CircuitCollectDto {

    private  Long circuitcollectId;

    private String circuitcollectName;

    private String circuitcollectCode;

    private String circuitcollectLength;

    private String circuitcollectFrequence;

    private String circuitcollectLatiPointA;

    private String circuitcollectLatiPointD;

    private String circuitcollectLongPointA;

    private String circuitcollectLongPointD;

    private String circuitcollectType;

    private String circuitcollectCat;

    private String circuitcollectRotation;

    private String circuitcollectSection;
    private String circuitcollectSectection;
}
