package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.model.Coordinate;
import sonaged.collecte.master.repository.CoordinateRepository;
import sonaged.collecte.master.repository.GeometryRepository;
import sonaged.collecte.master.dto.GeometryDto;
import sonaged.collecte.master.mapper.GeometryMapper;
import sonaged.collecte.master.model.Geometry;
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

    /**
     * @param geometryId
     * @return
     */
    @Override
    public GeometryDto getOneGeometry(Long geometryId) {
        Geometry geometry = geometryRepository.findById(geometryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Geometry with id [%s] not found ".formatted(geometryId)
                ));
        return GeometryMapper.GMP.modelToDto(geometry);
    }

    /**
     * @return
     */
    @Override
    public List<GeometryDto> getAllGeometry() {
        List<Geometry> geometryList = geometryRepository.findAll();
        return GeometryMapper.GMP.listModelToDto(geometryList);
    }

    /**
     * @param geometryDto
     * @return
     */
    @Override
    public GeometryDto createOneGeometry(GeometryDto geometryDto) {

        var geometry = geometryRepository.save(GeometryMapper.GMP.dtoToModel (geometryDto));
        //addExistedCoordinateToGeometry(geometryDto, geometry);
        //log.info("coodinate_232 {}",geometry.getCoordinates());
        return GeometryMapper.GMP.modelToDto(geometry);
    }

    /**
     * @param geometryId
     * @param geometryDto
     * @return
     */
    @Override
    public GeometryDto updateOneGeometry(Long geometryId, GeometryDto geometryDto) {
        Geometry existedGeometry = geometryRepository.findById(geometryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Geometry with id [%s] not found to update ".formatted(geometryId)
                ));
        if(geometryDto.getGeometryType() != null) {
            existedGeometry.setGeometryType(geometryDto.getGeometryType());
        }
        if(geometryDto.getGeometryRing() != null) {
            existedGeometry.setGeometryRing(geometryDto.getGeometryRing());
        }
        if(geometryDto.getSpatialReference() != null) {
            existedGeometry.setSpatialReference(geometryDto.getSpatialReference());
        }
        return GeometryMapper.GMP.modelToDto(geometryRepository.save(existedGeometry));
    }

    /**
     * @param geometryId
     */
    @Override
    public void deleteOneGeometry(Long geometryId) {
        Geometry geometry = geometryRepository.findById(geometryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Geometry with id [%s] not found ".formatted(geometryId)
                ));
        geometryRepository.delete(geometry);
    }

    private void addExistedCoordinateToGeometry(GeometryDto geometryDto, Geometry geometry){
        List<Coordinate> coordinates = geometryDto.getCoordinates();
       // List<Coordinate> _coordinates = new ArrayList<>();
        log.info("coodinate_1 {}", coordinates);
        if(!geometryDto.getCoordinates().isEmpty()){
            AtomicInteger i = new AtomicInteger();
            coordinates.forEach(coordinate -> {
                if (coordinate.getCoordinateId() != null){
                    Coordinate _coordinate = coordinateRepositoty.findById(coordinate.getCoordinateId()).get();
                    coordinates.set(i.get(),_coordinate);
                    log.info("coodinate_2 {}", i);
                }
                i.getAndIncrement();
            });
            geometry.setCoordinates(coordinates);
            log.info("coodinate_3 {}", i);
        }
    }
}
