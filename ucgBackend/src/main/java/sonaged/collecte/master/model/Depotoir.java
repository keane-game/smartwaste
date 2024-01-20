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
@Table(name = "SND_DEPOTOIR")
public class Depotoir {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "depotoirId")
    private  Long depotoirId;

    @Column(name = "depotoirAddress")
    private String depotoirAddress;

}
