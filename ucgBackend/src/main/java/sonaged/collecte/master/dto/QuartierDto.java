package sonaged.collecte.master.dto;


import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sonaged.collecte.master.model.Depotoir;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class QuartierDto {

    private  Long quartierId;

    private String quartierName;

    private String quartierCode;

    private String quartierCav;

    private String quartierCodeCav;

    private String quartierCcrca;

    private String quartierCodeCcrca;

    private String quartierCodeEntity;

    private String quartierNumerozr;

    private String quartierCodeSzr;

    private String quartierZoneCoron;

    private String quartierPoucentage;

    private String quartierLength;

    private String quartierArea;

    private List<Depotoir> depotoirs;
}
