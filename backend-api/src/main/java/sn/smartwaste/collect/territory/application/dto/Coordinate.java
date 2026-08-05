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

import java.io.Serializable;


/**
 * DTO de point géographique. <b>Exposé</b> ({@code @NamedInterface("geo")}). Voir la note de dette
 * sur {@code GeometryEntity}.
 */
@NamedInterface("geo")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Coordinate implements Serializable {

    UUID coordinateId;

    String latitude;

    String longitude;

    String altitude;

}
