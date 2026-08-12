package sn.smartwaste.collect.territory.application.dto;

import java.util.UUID;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Quartier implements Serializable {

    UUID quartierId;

    String name;

    String code;

    String cav;

    String codeCav;

    String cCrca;

    String codeCcrca;

    String codeEntity;

    String numerozr;

    String codeSzr;

    String zoneCoron;

    String poucentage;

    String length;

    String Area;

    // P1-2 : portait `CommuneEntity` (entité JPA exposée telle quelle), même défaut et même
    // correctif que `Commune.departmentId`.
    UUID communeId;

    Geometry geometry;

}
