package ucg.collecte.master.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "UCG_ALERT")
public class Alert {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alertId")
    private  Long alertId;

    @Column(name = "alertMessage", columnDefinition = "TEXT")
    private String alertMessage;

    @Column(name = "alertCode")
    private String alertCode;
}
