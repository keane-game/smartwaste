package sn.smartwaste.collect.territory.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.territory.application.dto.Coordinate;
import sn.smartwaste.collect.territory.domain.model.CoordinateEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface CoordinateMapper {

    CoordinateMapper CODMP = Mappers.getMapper(CoordinateMapper.class);

    Coordinate asDto(CoordinateEntity coordinate);

    CoordinateEntity asModel(Coordinate coordinateDto);

    List<Coordinate> asListDto(List<CoordinateEntity> coordinates);

}
