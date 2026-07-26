package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.mapper.GeometryMapper;
import sonaged.collecte.master.repository.GeometryRepository;
import sonaged.collecte.master.repository.RegionRepository;
import sonaged.collecte.master.dto.Region;
import sonaged.collecte.master.mapper.RegionMapper;
import sonaged.collecte.master.service.RegionService;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;
    private final GeometryRepository geometryRepository;


    @Override
    public Region readRegion(Long regionId) {
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
    public Region updateRegion(Long regionId, Region region) {
        return null;
    }


    @Override
    public void deleteRegion(Long regionId) {

    }
}
