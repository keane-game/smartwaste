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
@Table(name = "SND_TYPEDEPOTOIR")
public class TypeDepotoir {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "typeDepotoirId")
    private  Long typeDepotoirId;

    @Column(name = "typeDepotoirName")
    private String typeDepotoirName;
}
