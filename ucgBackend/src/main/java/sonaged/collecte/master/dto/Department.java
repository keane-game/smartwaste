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
public class Department {

    Long departmentId;

    String departmentName;

    String departmentCode;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    List<Commune> communes;

    Region region;

    Geometry geometry;
}
