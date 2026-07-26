package sonaged.collecte.master.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import sonaged.collecte.master.model.GeometryEntity;

import java.io.Serializable;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CircuitCollect implements Serializable {

    Long circuitcollectId;

    String name;

    String code;

    String length;

    String frequency;

    String latiPointA;

    String latiPointD;

    String longPointA;

    String longPointD;

    String type;

    String cat;

    String rotation;

    String Section;

    String Sectection;

    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    // P1-7 / ADR-0012 : commune référencée par identifiant (contexte distinct), plus par entité JPA.
    Long communeId;

    Geometry geometry;
}
