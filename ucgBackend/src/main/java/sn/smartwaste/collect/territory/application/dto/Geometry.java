package sn.smartwaste.collect.territory.application.dto;

import java.util.UUID;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import sn.smartwaste.collect.territory.domain.model.CoordinateEntity;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Geometry implements Serializable {

    UUID geometryId;

    String type;

    String spatialReference;

    String ring;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    List<CoordinateEntity> coordinates;
}
