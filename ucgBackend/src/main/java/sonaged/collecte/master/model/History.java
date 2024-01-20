package sonaged.collecte.master.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "SND_HISTORY")
public class History {

    @Id
    private  Long id;

}
