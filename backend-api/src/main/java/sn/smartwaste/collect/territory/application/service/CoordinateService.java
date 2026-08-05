package sn.smartwaste.collect.territory.application.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.smartwaste.collect.territory.application.dto.Coordinate;

import java.util.List;

public interface CoordinateService {
    Coordinate readCoordinate(UUID coordinateId);

    List<Coordinate> readAllCoordinate();

    Coordinate createCoordinate(Coordinate coordinate);

    Coordinate updateCoordinate(UUID CoordinateId, Coordinate coordinate);

    void deleteCoordinate(UUID coordinateId);

    Page<Coordinate> readAllCoordinate(Pageable pageable);
}
