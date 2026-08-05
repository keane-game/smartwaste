package sn.smartwaste.collect.territory.application.dto;

import java.util.UUID;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import sn.smartwaste.collect.territory.domain.model.CommuneEntity;
import sn.smartwaste.collect.territory.domain.model.GeometryEntity;

import java.io.Serializable;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Quartier implements Serializable {

    UUID quartierId;

    String name;

    String code;

    String cav;

    String codeCav;

    String cCrca;

    String codeCcrca;

    String codeEntity;

    String numerozr;

    String codeSzr;

    String zoneCoron;

    String poucentage;

    String length;

    String Area;

    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    CommuneEntity commune;

    Geometry geometry;

}
