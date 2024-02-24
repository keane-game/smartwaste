package sonaged.collecte.master.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sonaged.collecte.master.model.Department;


@Getter
@Setter
@AllArgsConstructor
public class CommuneDto {

    private  Long communeId;

    private String communeName;

    private String communeCode;

    private String residentTotal;

    private String womanResident;

    private String manResident;

    private String communeLength;

    private String communeArea;

    private Department department;
}
