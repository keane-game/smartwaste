package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.Coordinate;

import java.util.List;

public interface CoordinateService {
    Coordinate getOneCoordinate(Long coordinateId);

    List<Coordinate> getAllCoordinate();

    Coordinate createOneCoordinate(Coordinate coordinate);

    Coordinate updateOneCoordinate(Long CoordinateId, Coordinate coordinateDto);

    void deleteOneCoordinate(Long coordinateId);
}
