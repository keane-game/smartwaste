package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.Region;

import java.util.List;

public interface RegionService {

    Region readRegion(Long regionId);

    List<Region> readAllRegion();

    Region createRegion(Region region);

    Region updateRegion(Long regionId, Region region);

    void deleteRegion(Long regionId);
}
