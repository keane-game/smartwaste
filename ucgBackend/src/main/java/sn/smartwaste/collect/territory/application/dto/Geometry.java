package sn.smartwaste.collect.territory.application.dto;

import org.springframework.modulith.NamedInterface;

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

/**
 * DTO de contour géographique. <b>Exposé</b> ({@code @NamedInterface("geo")}) : les DTO du contexte
 * « Déchets » l'imbriquent. Voir la note de dette sur {@code GeometryEntity}.
 */
@NamedInterface("geo")
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
