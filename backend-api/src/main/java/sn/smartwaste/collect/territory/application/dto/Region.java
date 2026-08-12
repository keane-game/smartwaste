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
public class Region {

    UUID regionId;

    String name;

    String code;

    // P1-2 : portait `List<DepartmentEntity>` (entité JPA exposée telle quelle), même défaut
    // et même correctif que `Department.communeIds`.
    List<UUID> departmentIds;

    Geometry geometry;
}
