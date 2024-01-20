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
@Table(name = "SND_CIRCUIT")
public class Circuit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "circuitId")
    private  Long circuitId;

    @Column(name = "circuitName")
    private String circuitName;

    @Column(name = "circuitbalayageCode")
    private String circuitCode;
}
