package sn.smartwaste.collect.territory.domain.model;

import org.springframework.modulith.NamedInterface;

import org.hibernate.annotations.UuidGenerator;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

import java.util.UUID;

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;

import java.util.List;


/**
 * Contour géographique.
 *
 * <p><b>Exposé</b> ({@code @NamedInterface("geo")}) : la géométrie est une donnée de forme, pas une
 * donnée territoriale — les dépotoirs et circuits du contexte « Déchets » la <i>composent</i> (FK
 * {@code geometryid}, rétablie par le changelog 2.0.0), au même titre que les communes et
 * quartiers. C'est une composition et non une référence entre agrégats : l'ADR-0012 §1 range
 * explicitement les compositions {@code * → Geometry} dans le même contexte que leur porteur.
 *
 * <p><b>Dette assumée et localisée.</b> À terme, ces types géographiques ont leur place dans le
 * shared kernel, dont le {@code package-info} annonce déjà des « value objects géographiques ».
 * Le déplacement n'est pas fait ici parce qu'il déménagerait aussi repositories, DTO, mappers,
 * services et contrôleurs, et parce qu'un contrôleur n'a rien à faire dans un noyau partagé : la
 * décision de modélisation mérite d'être prise pour elle-même, pas au détour d'une migration.
 */
@NamedInterface("geo")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "GEOMETRY")
public class GeometryEntity extends AbstractAuditingEntity<UUID>  {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "GeometryId")
    UUID geometryId;

    @Column(name = "Type")
    String type;

    @Column(name = "SpatialReference")
    String spatialReference;

    @Column(name = "Ring")
    String ring;

    /**
     * Points du contour, <b>dans l'ordre où la source les a donnés</b>.
     *
     * <p>{@code @OrderBy} n'est pas décoratif ici : pour un polygone, l'ordre <b>est</b> la forme.
     * Les mêmes sommets relus dans un autre ordre décrivent une autre figure, sans lever la moindre
     * erreur — le rattachement territorial devient alors faux en silence. Sans cette annotation,
     * l'ordre de relecture était celui que la base voulait bien rendre : PostgreSQL rend souvent
     * les lignes dans l'ordre d'insertion, ce qui a suffi à faire fonctionner 69 rattachements sur
     * 71, mais c'est une coïncidence de plan d'exécution, pas une garantie.
     *
     * <p><b>Une position persistée, et non un tri sur l'identifiant.</b> Un premier correctif avait
     * posé {@code @OrderBy("coordinateId")}, en s'appuyant sur le fait que les identifiants sont des
     * UUID v7, ordonnés dans le temps. C'était faux, et pire que le défaut : l'horodatage d'un
     * UUID v7 a la granularité de la <b>milliseconde</b>, or les centaines de points d'un contour
     * sont insérés dans la même. Ils ne sont alors départagés que par des bits aléatoires, et le
     * tri devient délibérément aléatoire — le rattachement des points de collecte est tombé de
     * 70/71 à 52/71. Seule une position écrite en base dit l'ordre.
     */
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "geometryId")
    @jakarta.persistence.OrderColumn(name = "ringPosition")
    @ToString.Exclude
    @JsonIgnore
    List<CoordinateEntity> coordinates;

}
