package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.RegionDto;

import java.util.List;

public interface RegionService {

    RegionDto getOneRegion(Long regionId);

    List<RegionDto> getAllRegion();

    RegionDto createOneRegion(RegionDto regionDto);

    RegionDto updateOneRegion(Long regionId, RegionDto regionDto);
    void deleteOneRegion(Long regionId);
}
