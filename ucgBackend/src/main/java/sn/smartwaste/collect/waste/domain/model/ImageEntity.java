package sn.smartwaste.collect.waste.domain.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "IMAGE")
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ImageId")
    Long imageId;

    // P1-3 / ADR-0005 : les images ne sont plus stockées en BLOB. On ne conserve
    // en base qu'une référence vers le fichier stocké hors base (disque/objet).

    /** Nom du fichier stocké (clé relative dans le répertoire de stockage). */
    @Column(name = "Path", length = 1024)
    String path;

    /** URL publique de diffusion de l'image. */
    @Column(name = "Url", length = 1024)
    String url;

    /** Taille du fichier en octets. */
    @Column(name = "Size")
    Long size;

    /** Nom d'origine du fichier téléversé. */
    @Column(name = "Name")
    String name;

    /** Type MIME. */
    @Column(name = "Type")
    String type;

}