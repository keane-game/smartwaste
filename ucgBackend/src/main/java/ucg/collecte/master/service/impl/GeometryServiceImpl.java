package ucg.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ucg.collecte.master.dto.GeometryDto;
import ucg.collecte.master.exception.ResourceNotFoundException;
import ucg.collecte.master.mapper.GeometryMapper;
import ucg.collecte.master.mapper.QuartierMapper;
import ucg.collecte.master.model.Geometry;
import ucg.collecte.master.model.Quartier;
import ucg.collecte.master.repository.GeometryRepository;
import ucg.collecte.master.service.GeometryService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GeometryServiceImpl implements GeometryService {

    private final GeometryRepository geometryRepository;

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
        Geometry geometry = Geometry.builder()
                .geometryType(geometryDto.getGeometryType())
                .geometryRing(geometryDto.getGeometryRing())
                .spatialReference(geometryDto.getSpatialReference())
                .build();
        return GeometryMapper.GMP.modelToDto(geometryRepository.save(geometry));
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
}
