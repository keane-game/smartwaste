package sonaged.collecte.master.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DepotoirDto {

    private  Long depotoirId;

    private String depotoirAddress;
}
