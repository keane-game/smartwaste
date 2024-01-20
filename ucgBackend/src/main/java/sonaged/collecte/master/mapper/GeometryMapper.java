package sonaged.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.GeometryDto;
import sonaged.collecte.master.model.Geometry;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GeometryMapper {
    GeometryMapper GMP = Mappers.getMapper(GeometryMapper.class);

    GeometryDto modelToDto(Geometry geometry);

    Geometry dtoToModel (GeometryDto geometryDto);

    List<GeometryDto> listModelToDto(List<Geometry> geometries);
}
