package sn.smartwaste.collect.territory.application.service;

import java.util.UUID;

import sn.smartwaste.collect.territory.application.dto.Region;

import java.util.List;

public interface RegionService {

    Region readRegion(UUID regionId);

    List<Region> readAllRegion();

    Region createRegion(Region region);

    Region updateRegion(UUID regionId, Region region);

    void deleteRegion(UUID regionId);
}
