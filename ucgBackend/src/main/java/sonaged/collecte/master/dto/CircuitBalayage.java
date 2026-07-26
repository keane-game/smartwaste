package sonaged.collecte.master.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;


import java.io.Serializable;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CircuitBalayage implements Serializable {

    Long circuitbalayageId;

    String name;

    String code;

    String shift;

    String length;

    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    // P1-7 / ADR-0012 : commune référencée par identifiant (contexte distinct), plus par entité JPA.
    Long communeId;

    Geometry geometry;
}
