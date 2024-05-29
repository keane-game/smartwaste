package sonaged.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.Geometry;
import sonaged.collecte.master.model.GeometryEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GeometryMapper {
    GeometryMapper GMP = Mappers.getMapper(GeometryMapper.class);

    Geometry asDto(GeometryEntity geometry);

    GeometryEntity asModel(Geometry geometry);

    List<Geometry> asListDto(List<GeometryEntity> geometries);
}
