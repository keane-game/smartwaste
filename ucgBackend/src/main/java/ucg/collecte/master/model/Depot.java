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
@Table(name = "UCG_DEPOT")
public class Depot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "depotId")
    private  Long depotId;

    @Column(name = "depotAddress")
    private String depotAddress;

}
