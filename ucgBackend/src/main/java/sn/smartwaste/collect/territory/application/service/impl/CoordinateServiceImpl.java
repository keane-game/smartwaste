package sn.smartwaste.collect.territory.application.service.impl;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sn.smartwaste.collect.territory.application.dto.Coordinate;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sn.smartwaste.collect.territory.application.mapper.CoordinateMapper;
import sn.smartwaste.collect.territory.domain.repository.CoordinateRepository;
import sn.smartwaste.collect.territory.application.service.CoordinateService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CoordinateServiceImpl implements CoordinateService {


    private final CoordinateRepository coordinateRepository;


    @Override
    public Coordinate readCoordinate(UUID coordinateId) {
        var coordinate = coordinateRepository.findById(coordinateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Coordinate with id [%s] not found ".formatted(coordinateId)
                ));
        return CoordinateMapper.CODMP.asDto(coordinate);
    }


    @Override
    public List<Coordinate> readAllCoordinate() {
        var coordinateList = coordinateRepository.findByDeletionStatus(sonaged.collecte.master.enums.DeletionStatus.ACTIVE);
        return CoordinateMapper.CODMP.asListDto(coordinateList);
    }

    @Override
    public Page<Coordinate> readAllCoordinate(Pageable pageable){
        return coordinateRepository.findByDeletionStatus (sonaged.collecte.master.enums.DeletionStatus.ACTIVE, pageable).map(CoordinateMapper.CODMP::asDto);
    }


    @Override
    public Coordinate createCoordinate(Coordinate coordinate) {
        var savedCoordinate =  coordinateRepository.save(CoordinateMapper.CODMP.asModel (coordinate));
        return CoordinateMapper.CODMP.asDto (savedCoordinate);
    }


    @Override
    public Coordinate updateCoordinate(UUID coordinateId, Coordinate coordinate) {
        var existedCoordinate = coordinateRepository.findById(coordinateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Coordinate with id [%s] not found ".formatted(coordinateId)
                ));
        if (coordinate.getLatitude() != null){
            existedCoordinate.setLatitude(coordinate.getLatitude());
        }
        if (coordinate.getLongitude() != null){
            existedCoordinate.setLongitude(coordinate.getLongitude());
        }
        if (coordinate.getAltitude() != null){
            existedCoordinate.setAltitude(coordinate.getAltitude());
        }
        return CoordinateMapper.CODMP.asDto(existedCoordinate);
    }


    @Override
    public void deleteCoordinate(UUID coordinateId) {
        var coordinate = coordinateRepository.findById(coordinateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Coordinate with id [%s] not found ".formatted(coordinateId)
                ));
        coordinate.markForDeletion(java.time.LocalDateTime.now()); coordinateRepository.save(coordinate); // soft-delete (rétention + purge planifiée)
    }
}
