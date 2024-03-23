package sonaged.collecte.master.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sonaged.collecte.master.model.Depotoir;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TypeDepotoirDto {

    private  Long typeDepotoirId;

    private String typeDepotoirName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Depotoir> depotoirs;
}
