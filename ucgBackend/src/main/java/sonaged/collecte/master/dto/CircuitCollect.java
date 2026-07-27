package sonaged.collecte.master.dto;

import java.util.UUID;

// Les DTO du référentiel territorial ont migré vers leur module dédié : ils ne sont plus
// résolus par appartenance au même package et exigent un import explicite.
import sn.smartwaste.collect.territory.application.dto.Geometry;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import sn.smartwaste.collect.territory.domain.model.GeometryEntity;

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
    UUID communeId;

    Geometry geometry;
}
