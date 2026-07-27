package sonaged.collecte.master.dto;

import java.util.UUID;

// Les DTO du référentiel territorial ont migré vers leur module dédié : ils ne sont plus
// résolus par appartenance au même package et exigent un import explicite.
import sn.smartwaste.collect.territory.application.dto.Coordinate;
import sn.smartwaste.collect.territory.application.dto.Geometry;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import sonaged.collecte.master.enums.DeletionStatus;

import sn.smartwaste.collect.territory.domain.model.GeometryEntity;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Depotoir {

    Long depotoirId;

    String address;

    // P1-6 / ADR-0012 : quartier référencé par identifiant (contexte distinct), pas par objet.
    UUID quartierId;

    TypeDepotoir typeDepotoir;

    // P1-7 / ADR-0012 : la commune était exposée sous forme d'ENTITÉ JPA dans le DTO
    // (fuite du modèle + couplage cross-contexte). Remplacée par son identifiant ;
    // les clients résolvent la commune via l'API du référentiel (`/v1/communes/{id}`).
    UUID communeId;

    Geometry geometry;

    List<Coordinate> coordinates;

    // Soft-delete : statut + horodatage (mappés depuis l'entité) et date de purge prévue (calculée).
    DeletionStatus deletionStatus;
    LocalDateTime deletionRequestedAt;
    LocalDateTime purgeDueAt;
}
