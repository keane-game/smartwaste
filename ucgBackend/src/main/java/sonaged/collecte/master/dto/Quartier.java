package sonaged.collecte.master.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Quartier implements Serializable {

    Long id;

    String name;

    String code;

    String cav;

    String codeCav;

    String cCrca;

    String codeCcrca;

    String codeEntity;

    String numerozr;

    String codeSzr;

    String zoneCoron;

    String poucentage;

    String length;

    String Area;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    List<Depotoir> depotoirs;

    Commune commune;

    Geometry geometry;
}
