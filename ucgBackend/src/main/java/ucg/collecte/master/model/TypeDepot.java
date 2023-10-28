package ucg.collecte.master.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "UCG_TYPEDEPOT")
public class TypeDepot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "typeDepotId")
    private  Long typeDepotId;

    @Column(name = "typeDepotName")
    private String typeDepotName;
}
