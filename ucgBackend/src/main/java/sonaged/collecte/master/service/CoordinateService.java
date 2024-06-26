package sonaged.collecte.master.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sonaged.collecte.master.dto.Coordinate;

import java.util.List;

public interface CoordinateService {
    Coordinate readCoordinate(Long coordinateId);

    List<Coordinate> readAllCoordinate();

    Coordinate createCoordinate(Coordinate coordinate);

    Coordinate updateCoordinate(Long CoordinateId, Coordinate coordinate);

    void deleteCoordinate(Long coordinateId);

    Page<Coordinate> readAllCoordinate(Pageable pageable);
}
