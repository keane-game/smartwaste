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
@Table(name = "UCG_QUARTIER")
public class Quartier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quartierId")
    private  Long qquartierId;

    @Column(name = "quartierName")
    private String quartierName;

    @Column(name = "quartierCode")
    private String quartierCode;

    @Column(name = "quartierCav")
    private String quartierCav;

    @Column(name = "quartierCodeCav")
    private String quartierCodeCav;

    @Column(name = "quartierCcrca")
    private String quartierCcrca;

    @Column(name = "quartierCodeCcrca")
    private String quartierCodeCcrca;


    @Column(name = "quartierCodeEntity")
    private String quartierCodeEntity;

    @Column(name = "quartierNumerozr")
    private String quartierNumerozr;

    @Column(name = "quartierCodeSzr")
    private String quartierCodeSzr;

    @Column(name = "quartierZoneCoron")
    private String quartierZoneCoron;

    @Column(name = "quartierPoucentage")
    private String quartierPoucentage;

    @Column(name = "quartierLength")
    private String quartierLength;

    @Column(name = "quartierArea")
    private String quartierArea;

}
