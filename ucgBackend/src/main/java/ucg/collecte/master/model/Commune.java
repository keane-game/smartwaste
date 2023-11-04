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
@Table(name = "UCG_COMMUNE")
public class Commune {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "communeId")
    private  Long communeId;

    @Column(name = "communeName")
    private String communeName;

    @Column(name = "communeCode")
    private String communeCode;

    @Column(name = "totalResident")
    private String residentTotal;

    @Column(name = "womanResident")
    private String womanResident;

    @Column(name = "manResident")
    private String manResident;

    @Column(name = "communeLength")
    private String communeLength;

    @Column(name = "communeArea")
    private String communeArea;
}
