package sn.smartwaste.collect.waste.application.api;

// Les DTO du référentiel territorial ont migré vers leur module dédié : ils ne sont plus
// résolus par appartenance au même package et exigent un import explicite.
import sn.smartwaste.collect.territory.application.dto.Coordinate;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DepotoirMaps {

    String address;

    String typeDepot;

    String typeGeo;

    List<Coordinate> coordinates;

    /** {@code null} si jamais mesuré — un dépotoir sans capteur n'a pas de niveau à afficher. */
    Integer fillLevelPercent;

    /** Date de {@link #fillLevelPercent}, {@code null} dans les mêmes conditions. */
    Instant lastMeasuredAt;

}
