package sn.smartwaste.collect.platform.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "avis")
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String message;
    private String statut;

    /**
     * Auteur de l'avis, référencé <b>par identifiant</b> et non par association JPA : le contexte
     * « Identité &amp; Accès » est un autre bounded context (ADR-0012 / ADR-0013 §3). Aucune FK SQL
     * ne traverse la frontière — la valeur provient du principal authentifié, donc son existence
     * est acquise à l'écriture et n'a pas à être revalidée.
     */
    @Column(name = "userId")
    private UUID userId;

}
