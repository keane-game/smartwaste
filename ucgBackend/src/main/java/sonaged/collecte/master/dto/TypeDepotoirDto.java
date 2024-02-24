package sonaged.collecte.master.dto;


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

    private List<Depotoir> depotoirs;
}
