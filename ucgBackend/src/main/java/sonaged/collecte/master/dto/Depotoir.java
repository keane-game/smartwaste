package sonaged.collecte.master.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import sonaged.collecte.master.model.CommuneEntity;
import sonaged.collecte.master.model.GeometryEntity;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Depotoir {

    Long depotoirId;

    String address;

    TypeDepotoir typeDepotoir;

    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    CommuneEntity commune;

    Geometry geometry;

    List<Coordinate> coordinates;
}
