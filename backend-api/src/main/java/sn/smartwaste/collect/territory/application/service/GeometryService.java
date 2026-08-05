package sn.smartwaste.collect.territory.application.service;

import java.util.UUID;

import sn.smartwaste.collect.territory.application.dto.Geometry;

import java.util.List;

public interface GeometryService {
    Geometry readGeometry(UUID geometryId);

    List<Geometry> readAllGeometry();

    Geometry createGeometry(Geometry geometry);

    Geometry updateGeometry(UUID geometryId, Geometry geometry);

    void deleteGeometry(UUID geometryId);

}
