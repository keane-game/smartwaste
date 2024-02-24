package sonaged.collecte.master.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sonaged.collecte.master.model.Commune;
import sonaged.collecte.master.model.Region;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DepartmentDto {

    private  Long departmentId;

    private String departmentName;

    private String departmentCode;

    private List<Commune> communes;

    private Region region;
}
