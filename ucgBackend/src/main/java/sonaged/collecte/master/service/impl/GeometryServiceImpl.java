package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.dto.Coordinate;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.repository.CoordinateRepository;
import sonaged.collecte.master.repository.GeometryRepository;
import sonaged.collecte.master.dto.Geometry;
import sonaged.collecte.master.mapper.GeometryMapper;

import sonaged.collecte.master.service.GeometryService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Service
@Slf4j
public class GeometryServiceImpl implements GeometryService {

    private final GeometryRepository geometryRepository;
    private final CoordinateRepository coordinateRepositoty;


    @Override
    public Geometry readGeometry(Long geometryId) {
        var geometry = geometryRepository.findById(geometryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Geometry with id [%s] not found ".formatted(geometryId)
                ));
        return GeometryMapper.GMP.asDto(geometry);
    }


    @Override
    public List<Geometry> readAllGeometry() {
        var geometryList = geometryRepository.findAll();
        return GeometryMapper.GMP.asListDto(geometryList);
    }


    @Override
    public Geometry createGeometry(Geometry geometry) {
        var savedGeometry = geometryRepository.save(GeometryMapper.GMP.asModel (geometry));
        return GeometryMapper.GMP.asDto(savedGeometry);
    }


    @Override
    public Geometry updateGeometry(Long geometryId, Geometry geometry) {
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
    public void deleteGeometry(Long geometryId) {
        var geometry = geometryRepository.findById(geometryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Geometry with id [%s] not found ".formatted(geometryId)
                ));
        geometryRepository.delete(geometry);
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
