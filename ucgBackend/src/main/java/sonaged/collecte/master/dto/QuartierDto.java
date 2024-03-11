package sonaged.collecte.master.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sonaged.collecte.master.model.Depotoir;
import sonaged.collecte.master.model.Geometry;

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

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Depotoir> depotoirs;

    private CommuneDto commune;

    private GeometryDto geometry;
}
