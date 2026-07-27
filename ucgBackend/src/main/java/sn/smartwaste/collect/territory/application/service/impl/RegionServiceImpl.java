package sn.smartwaste.collect.territory.application.service.impl;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sn.smartwaste.collect.territory.application.mapper.GeometryMapper;
import sn.smartwaste.collect.territory.domain.repository.GeometryRepository;
import sn.smartwaste.collect.territory.domain.repository.RegionRepository;
import sn.smartwaste.collect.territory.application.dto.Region;
import sn.smartwaste.collect.territory.application.mapper.RegionMapper;
import sn.smartwaste.collect.territory.application.service.RegionService;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;
    private final GeometryRepository geometryRepository;


    @Override
    public Region readRegion(UUID regionId) {
        var region  = regionRepository.findById(regionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found ".formatted(regionId)
                ));
        return RegionMapper.RMP.asDto(region);
    }


    @Override
    public List<Region> readAllRegion() {
        var regionList = regionRepository.findByDeletionStatus(sonaged.collecte.master.enums.DeletionStatus.ACTIVE);
        return RegionMapper.RMP.asListDto(regionList);
    }


    @Override
    public Region createRegion(Region region) {

        var savedRegion = regionRepository.save(RegionMapper.RMP.asModel(region));
        return RegionMapper.RMP.asDto(savedRegion);
    }


    @Override
    public Region updateRegion(UUID regionId, Region region) {
        return null;
    }


    @Override
    public void deleteRegion(UUID regionId) {

    }
}
