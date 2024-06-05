package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.Geometry;

import java.util.List;

public interface GeometryService {
    Geometry readGeometry(Long geometryId);

    List<Geometry> readAllGeometry();

    Geometry createGeometry(Geometry geometry);

    Geometry updateGeometry(Long geometryId, Geometry geometry);

    void deleteGeometry(Long geometryId);
}
