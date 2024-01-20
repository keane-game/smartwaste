package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.CoordinateDto;

import java.util.List;

public interface CoordinateService {
    CoordinateDto getOneCoordinate(Long coordinateId);

    List<CoordinateDto> getAllCoordinate();

    CoordinateDto createOneCoordinate(CoordinateDto coordinateDto);

    CoordinateDto updateOneCoordinate(Long CoordinateId, CoordinateDto coordinateDto);

    void deleteOneCoordinate(Long coordinateId);
}
