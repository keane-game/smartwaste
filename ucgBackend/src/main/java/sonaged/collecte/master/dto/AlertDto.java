package sonaged.collecte.master.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sonaged.collecte.master.enums.AlertCode;


@Getter
@Setter
@AllArgsConstructor
public class AlertDto {
    private  Long alertId;

    private String alertObject;

    private String alertMessage;

    private AlertCode alertCode;


}
