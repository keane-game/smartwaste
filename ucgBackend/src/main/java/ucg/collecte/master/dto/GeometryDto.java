package ucg.collecte.master.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GeometryDto {
    private  Long geometryId;

    private String geometryType;


    private String spatialReference;


    private String geometryRing;
}
