package sn.smartwaste.collect.territory.application.dto;

import java.util.UUID;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Department {

    UUID departmentId;

    String name;

    String code;

    // P1-2 : portait `List<CommuneEntity>`/`RegionEntity` (entités JPA exposées telles quelles
    // dans le DTO), ce qui forçait un chargement complet des associations à chaque lecture et
    // fuitait le modèle de persistance par l'API. Même patron que `Commune.departmentId`.
    List<UUID> communeIds;

    UUID regionId;

    Geometry geometry;


}
