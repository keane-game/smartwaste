package ucg.collecte.master.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


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
}
