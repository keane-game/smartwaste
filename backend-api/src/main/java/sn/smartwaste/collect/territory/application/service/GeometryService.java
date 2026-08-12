package sn.smartwaste.collect.territory.application.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.smartwaste.collect.territory.application.dto.Geometry;

import java.util.List;

public interface GeometryService {
    Geometry readGeometry(UUID geometryId);

    List<Geometry> readAllGeometry();

    /** Corrige un gap relevé par audit (2026-08-10) : ressource sans pagination. */
    Page<Geometry> readAllGeometry(Pageable pageable);

    Geometry createGeometry(Geometry geometry);

    Geometry updateGeometry(UUID geometryId, Geometry geometry);

    void deleteGeometry(UUID geometryId);

}
