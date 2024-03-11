package sonaged.collecte.master.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sonaged.collecte.master.model.Commune;
import sonaged.collecte.master.model.Department;
import sonaged.collecte.master.model.Geometry;
import sonaged.collecte.master.model.Quartier;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
public class CommuneDto {

    private  Long communeId;

    private String communeName;

    private String communeCode;

    private String residentTotal;

    private String womanResident;

    private String manResident;

    private String communeLength;

    private String communeArea;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Quartier> quartiers;

    private DepartmentDto department;

    private GeometryDto geometry;
}
