package ucg.collecte.master.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CircuitBalayageDto {

    private  Long circuitbalayageId;


    private String circuitbalayageName;


    private String circuitbalayageCode;


    private String circuitbalayageShift;


    private String circuitbalayageLength;
}
