package sonaged.collecte.master.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SND_MOBLIERURBAIN")
public class MoblierUrbain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "moblierUrbainId")
    private  Long moblierUrbainId;

    @Column(name = "moblierUrbainName")
    private String moblierUrbainName;

    @Column(name = "moblierUrbainCode")
    private String moblierUrbainCode;
}
