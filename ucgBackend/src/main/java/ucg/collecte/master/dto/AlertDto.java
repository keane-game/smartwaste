package ucg.collecte.master.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class AlertDto {
    private  Long alertId;

    private String alertMessage;

    private String alertCode;
}
