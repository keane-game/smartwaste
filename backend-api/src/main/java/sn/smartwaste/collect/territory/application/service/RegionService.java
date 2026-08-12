package sn.smartwaste.collect.territory.application.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.smartwaste.collect.territory.application.dto.Region;

import java.util.List;

public interface RegionService {

    Region readRegion(UUID regionId);

    List<Region> readAllRegion();

    /** Corrige un gap relevé par audit (2026-08-10) : seule ressource territoriale sans pagination. */
    Page<Region> readAllRegion(Pageable pageable);

    Region createRegion(Region region);

    Region updateRegion(UUID regionId, Region region);

    void deleteRegion(UUID regionId);
}
