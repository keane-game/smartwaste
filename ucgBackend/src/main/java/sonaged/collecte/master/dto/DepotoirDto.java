package sonaged.collecte.master.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sonaged.collecte.master.model.Quartier;
import sonaged.collecte.master.model.TypeDepotoir;

@Getter
@Setter
@AllArgsConstructor
public class DepotoirDto {

    private  Long depotoirId;

    private String depotoirAddress;

    private TypeDepotoir typeDepotoir;

    private Quartier quartier;
}
