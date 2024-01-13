package ucg.collecte.master.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DepartementDto {

    private  Long departementId;

    private String departementName;

    private String departementCode;
}
