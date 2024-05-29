package sonaged.collecte.master.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Depotoir {

    private  Long depotoirId;

    private String depotoirAddress;

    private Geometry geometry;

    private TypeDepotoir typeDepotoir;

    private Quartier quartier;
}
