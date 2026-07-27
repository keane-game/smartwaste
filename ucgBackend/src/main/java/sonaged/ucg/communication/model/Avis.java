package sonaged.ucg.communication.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
// Import devenu explicite : Avis vivait dans le même package qu'UserEntity avant la
// migration modulaire (P1-7b). Référence cross-contexte vers « Identité & Accès »,
// à convertir en `userId` (ADR-0012) quand ce contexte sera migré à son tour.
import sonaged.collecte.master.model.UserEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "avis")
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String message;
    private String statut;
    @ManyToOne
    private UserEntity user;

}
