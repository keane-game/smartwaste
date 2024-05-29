package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.Region;

import java.util.List;

public interface RegionService {

    Region getOneRegion(Long regionId);

    List<Region> getAllRegion();

    Region createOneRegion(Region region);

    Region updateOneRegion(Long regionId, Region region);
    void deleteOneRegion(Long regionId);
}
