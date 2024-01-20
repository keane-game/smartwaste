package sonaged.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.CoordinateDto;
import sonaged.collecte.master.model.Coordinate;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CoordinateMapper {

    CoordinateMapper COORDINATE_MAPPER = Mappers.getMapper(CoordinateMapper.class);

    CoordinateDto modelToDto(Coordinate coordinate);

    Coordinate dtoToModel (CoordinateDto coordinateDto);

    List<CoordinateDto> listModelToDto(List<Coordinate> coordinates);

}
