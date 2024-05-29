package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.Geometry;

import java.util.List;

public interface GeometryService {
    Geometry getOneGeometry(Long geometryId);

    List<Geometry> getAllGeometry();

    Geometry createOneGeometry(Geometry geometry);

    Geometry updateOneGeometry(Long geometryId, Geometry geometry);
    void deleteOneGeometry(Long geometryId);
}
