package sonaged.collecte.master.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import sonaged.collecte.master.model.CoordinateEntity;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Geometry implements Serializable {

    Long geometryId;

    String type;

    String spatialReference;

    String ring;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    List<CoordinateEntity> coordinates;
}
