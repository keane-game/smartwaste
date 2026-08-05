package sn.smartwaste.collect.waste.application.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Image {


    private Long imageId;

    // P1-3 / ADR-0005 : référence vers le fichier (plus de BLOB `data`).
    private String path;
    private String url;
    private Long size;

    private String name;
    private String type;
}