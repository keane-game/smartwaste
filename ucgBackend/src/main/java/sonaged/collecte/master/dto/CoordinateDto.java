package sonaged.collecte.master.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sonaged.collecte.master.model.Geometry;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class CoordinateDto implements Serializable {

    private Long coordinateId;

    private String latitude;

    private String longitude;

    private String altitude;

    private Geometry geometry;
}
