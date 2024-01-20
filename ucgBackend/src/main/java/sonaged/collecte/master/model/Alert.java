package sonaged.collecte.master.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sonaged.collecte.master.enums.AlertCode;

import java.io.Serial;
import java.io.Serializable;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "SND_ALERT")
public class Alert implements Serializable {


    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alertId")
    private  Long alertId;

    @Column(name = "alertObject")
    private String alertObject;

    @Column(name = "alertMessage", columnDefinition = "TEXT")
    private String alertMessage;

    @Enumerated(EnumType.STRING)
    @Column(name="alertCode")
    private AlertCode alertCode;
}
