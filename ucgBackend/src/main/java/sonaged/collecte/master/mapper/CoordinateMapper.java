package sonaged.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.Coordinate;
import sonaged.collecte.master.model.CoordinateEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface CoordinateMapper {

    CoordinateMapper CODMP = Mappers.getMapper(CoordinateMapper.class);

    Coordinate asDto(CoordinateEntity coordinate);

    CoordinateEntity asModel(Coordinate coordinateDto);

    List<Coordinate> asListDto(List<CoordinateEntity> coordinates);

}
