package sonaged.collecte.master.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sonaged.collecte.master.model.Commune;
import sonaged.collecte.master.model.Coordinate;
import sonaged.collecte.master.model.Geometry;
import sonaged.collecte.master.model.Region;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;
import static org.apache.coyote.http11.Constants.a;

@Getter
@Setter
@AllArgsConstructor
public class DepartmentDto {

    private  Long departmentId;

    private String departmentName;

    private String departmentCode;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Commune> communes;

    private RegionDto region;

    private GeometryDto geometry;
}
