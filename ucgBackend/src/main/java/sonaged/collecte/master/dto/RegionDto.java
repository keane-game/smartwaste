package sonaged.collecte.master.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sonaged.collecte.master.model.Department;
import sonaged.collecte.master.model.Depotoir;
import sonaged.collecte.master.model.Geometry;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RegionDto {

    private  Long regionId;

    private String regionName;

    private String regionCode;

    private GeometryDto geometry;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Department> departments;

}
