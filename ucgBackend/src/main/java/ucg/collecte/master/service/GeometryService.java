package ucg.collecte.master.service;

import ucg.collecte.master.dto.GeometryDto;

import java.util.List;

public interface GeometryService {
    GeometryDto getOneGeometry(Long geometryId);

    List<GeometryDto> getAllGeometry();

    GeometryDto createOneGeometry(GeometryDto geometryDto);

    GeometryDto updateOneGeometry(Long geometryId, GeometryDto geometryDto);
    void deleteOneGeometry(Long geometryId);
}
