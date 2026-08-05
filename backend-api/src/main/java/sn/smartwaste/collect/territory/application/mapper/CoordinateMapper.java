package sn.smartwaste.collect.territory.application.mapper;

import org.springframework.modulith.NamedInterface;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.territory.application.dto.Coordinate;
import sn.smartwaste.collect.territory.domain.model.CoordinateEntity;

import java.util.List;

/**
 * <b>Exposé</b> ({@code @NamedInterface("geo")}) : les mappers du contexte « Déchets » délèguent la
 * conversion des coordonnées. Voir la note de dette sur {@code GeometryEntity}.
 */
@NamedInterface("geo")
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface CoordinateMapper {

    CoordinateMapper CODMP = Mappers.getMapper(CoordinateMapper.class);

    Coordinate asDto(CoordinateEntity coordinate);

    CoordinateEntity asModel(Coordinate coordinateDto);

    List<Coordinate> asListDto(List<CoordinateEntity> coordinates);

}
