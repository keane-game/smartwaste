package ucg.collecte.master.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DepotDto {

    private  Long depotId;

    private String depotAddress;
}
