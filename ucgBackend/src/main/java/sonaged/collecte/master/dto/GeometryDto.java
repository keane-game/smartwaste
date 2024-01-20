package sonaged.collecte.master.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.internal.util.collections.LazyIndexedMap;
import sonaged.collecte.master.model.Coordinate;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class GeometryDto implements Serializable {
    private  Long geometryId;

    private String geometryType;

    private String spatialReference;

    private String geometryRing;

    private List<Coordinate> coordinates;
}
