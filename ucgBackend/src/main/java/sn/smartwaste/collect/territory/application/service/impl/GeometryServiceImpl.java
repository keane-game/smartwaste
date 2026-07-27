package sn.smartwaste.collect.territory.application.service.impl;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sn.smartwaste.collect.territory.application.dto.Coordinate;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sn.smartwaste.collect.territory.domain.repository.CoordinateRepository;
import sn.smartwaste.collect.territory.domain.repository.GeometryRepository;
import sn.smartwaste.collect.territory.application.dto.Geometry;
import sn.smartwaste.collect.territory.application.mapper.GeometryMapper;

import sn.smartwaste.collect.territory.application.service.GeometryService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Service
@Slf4j
public class GeometryServiceImpl implements GeometryService {

    private final GeometryRepository geometryRepository;
    private final CoordinateRepository coordinateRepository;


    @Override
    public Geometry readGeometry(UUID geometryId) {
        var geometry = geometryRepository.findById(geometryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Geometry with id [%s] not found ".formatted(geometryId)
                ));
        return GeometryMapper.GMP.asDto(geometry);
    }


    @Override
    public List<Geometry> readAllGeometry() {
        var geometryList = geometryRepository.findByDeletionStatus(sonaged.collecte.master.enums.DeletionStatus.ACTIVE);
        return GeometryMapper.GMP.asListDto(geometryList);
    }


    @Override
    public Geometry createGeometry(Geometry geometry) {
        var savedGeometry = geometryRepository.save(GeometryMapper.GMP.asModel (geometry));
        return GeometryMapper.GMP.asDto(savedGeometry);
    }


    @Override
    public Geometry updateGeometry(UUID geometryId, Geometry geometry) {
        var existedGeometry = geometryRepository.findById(geometryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Geometry with id [%s] not found to update ".formatted(geometryId)
                ));
        if(geometry.getType() != null) {
            existedGeometry.setType(geometry.getType());
        }
        if(geometry.getRing() != null) {
            existedGeometry.setRing(geometry.getRing());
        }
        if(geometry.getSpatialReference() != null) {
            existedGeometry.setSpatialReference(geometry.getSpatialReference());
        }
        return GeometryMapper.GMP.asDto(geometryRepository.save(existedGeometry));
    }


    @Override
    public void deleteGeometry(UUID geometryId) {
        var geometry = geometryRepository.findById(geometryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Geometry with id [%s] not found ".formatted(geometryId)
                ));
        geometry.markForDeletion(java.time.LocalDateTime.now()); geometryRepository.save(geometry); // soft-delete (rétention + purge planifiée)
    }
/*
    private void addExistedCoordinateToGeometry(GeometryEntity geometryEntity, Geometry geometry){
        List<Coordinate> coordinates = geometry.getCoordinates();
       // List<Coordinate> _coordinates = new ArrayList<>();
        log.info("coodinate_1 {}", coordinates);
        if(!geometry.getCoordinates().isEmpty()){
            AtomicInteger i = new AtomicInteger();
            coordinates.forEach(coordinate -> {
                if (coordinate.getCoordinateId() != null){
                    CoordinateEntity _coordinate = coordinateRepositoty.findById(coordinate.getCoordinateId()).get();
                    //coordinates.set(i.get(), _coordinate);
                    log.info("coodinate_2 {}", i);
                }
                i.getAndIncrement();
            });
            geometry.setCoordinates(coordinates);
            log.info("coodinate_3 {}", i);
        }
    }*/
}
