package sonaged.collecte.master.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import sonaged.collecte.master.model.DepartmentEntity;
import sonaged.collecte.master.model.DepotoirEntity;
import sonaged.collecte.master.model.GeometryEntity;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Commune {

    Long communeId;

    String name;

    String code;

    String total;

    String women;

    String men;

    String length;

    String area;

   /* @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    List<Quartier> quartiers;*/

    List<DepotoirEntity> depotoirs;

    DepartmentEntity department;

    Geometry geometry;


}
