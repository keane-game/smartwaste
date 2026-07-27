package sn.smartwaste.collect.territory.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.territory.application.dto.Geometry;
import sn.smartwaste.collect.territory.domain.model.GeometryEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GeometryMapper {
    GeometryMapper GMP = Mappers.getMapper(GeometryMapper.class);

    Geometry asDto(GeometryEntity geometry);

    GeometryEntity asModel(Geometry geometry);

    List<Geometry> asListDto(List<GeometryEntity> geometries);
}
