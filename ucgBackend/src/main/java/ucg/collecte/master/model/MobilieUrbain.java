package ucg.collecte.master.model;

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
@Table(name = "UCG_MOBILIEURBAIN")
public class MobilieUrbain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mobilieUrbainId")
    private  Long mobilieUrbainId;

    @Column(name = "mobilieUrbainName")
    private String mobilieUrbainName;

    @Column(name = "mobilieUrbainCode")
    private String mobilieUrbainCode;
}
