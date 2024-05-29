package sonaged.collecte.master.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Commune {

    Long id;

    String name;

    String code;

    String residentTotal;

    String womanResident;

    String manResident;

    String length;

    String area;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    List<Quartier> quartiers;

    Department department;

    Geometry geometry;
}
