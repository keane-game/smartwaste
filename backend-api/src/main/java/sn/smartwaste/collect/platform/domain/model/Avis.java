package sn.smartwaste.collect.platform.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "avis")
public class Avis {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    private UUID id;

    /** Description libre du signalement, saisie par l'habitant. */
    private String message;

    /**
     * État du traitement. Auparavant une chaîne libre que rien ne faisait évoluer : l'habitant
     * pouvait signaler, personne ne pouvait clore.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private AvisStatus statut = AvisStatus.SIGNALE;

    // ---- Localisation du signalement ----
    // Un depot sauvage sans position n'est pas exploitable : impossible de l'afficher sur la
    // carte de supervision, ni d'envoyer une equipe. Le memoire en fait un cas d'usage citoyen
    // explicite (« envoyer une alerte pour signaler un depot sauvage »).
    // Chaines et non doubles, par coherence avec CoordinateEntity du referentiel.

    @Column(name = "latitude")
    private String latitude;

    @Column(name = "longitude")
    private String longitude;

    /**
     * Date de dépôt.
     *
     * <p>L'entité n'en avait <b>aucune</b> : impossible de dire quand un signalement avait été fait,
     * donc impossible de mesurer un délai de traitement ou de repérer ceux qu'on oublie. C'est ce
     * qui manquait pour rendre compte du service réellement rendu aux habitants.
     */
    @Column(name = "submittedAt", nullable = false)
    private java.time.Instant submittedAt;

    // ---- Traitement ----

    @Column(name = "processedAt")
    private java.time.Instant processedAt;

    /** Agent ayant clos le signalement — reference par identifiant (ADR-0012). */
    @Column(name = "processedByUserId")
    private UUID processedByUserId;

    /**
     * Auteur de l'avis, référencé <b>par identifiant</b> et non par association JPA : le contexte
     * « Identité &amp; Accès » est un autre bounded context (ADR-0012 / ADR-0013 §3). Aucune FK SQL
     * ne traverse la frontière — la valeur provient du principal authentifié, donc son existence
     * est acquise à l'écriture et n'a pas à être revalidée.
     */
    @Column(name = "userId")
    private UUID userId;

}
