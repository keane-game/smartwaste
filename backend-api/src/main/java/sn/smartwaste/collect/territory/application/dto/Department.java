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
import sn.smartwaste.collect.territory.domain.model.RegionEntity;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Department {

    UUID departmentId;

    String name;

    String code;

    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    List<CommuneEntity> communes;

    RegionEntity region;

    Geometry geometry;


}
