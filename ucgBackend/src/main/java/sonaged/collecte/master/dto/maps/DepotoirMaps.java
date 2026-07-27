package sonaged.collecte.master.dto;

// Les DTO du référentiel territorial ont migré vers leur module dédié : ils ne sont plus
// résolus par appartenance au même package et exigent un import explicite.
import sn.smartwaste.collect.territory.application.dto.Coordinate;


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
public class DepotoirMaps {

    String address;

    String typeDepot;

    String typeGeo;

    List<Coordinate> coordinates;

}
