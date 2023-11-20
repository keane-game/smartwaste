package ucg.collecte.master.model;


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
@Table(name = "UCG_CIRCUITBALAYAGE")
public class CircuitBalayage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "circuitbalayageId")
    private  Long circuitbalayageId;

    @Column(name = "circuitbalayageName")
    private String circuitbalayageName;

    @Column(name = "circuitbalayageCode")
    private String circuitbalayageCode;

    @Column(name = "circuitbalayageShift")
    private String circuitbalayageShift;

    @Column(name = "circuitbalayageLength")
    private String circuitbalayageLength;
}
