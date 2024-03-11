package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.mapper.GeometryMapper;
import sonaged.collecte.master.repository.GeometryRepository;
import sonaged.collecte.master.repository.RegionRepository;
import sonaged.collecte.master.dto.RegionDto;
import sonaged.collecte.master.mapper.RegionMapper;
import sonaged.collecte.master.model.Region;
import sonaged.collecte.master.service.RegionService;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;
    private final GeometryRepository geometryRepository;
    /**
     * @param regionId 
     * @return
     */
    @Override
    public RegionDto getOneRegion(Long regionId) {
        Region region  = regionRepository.findById(regionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found ".formatted(regionId)
                ));
        return RegionMapper.RMP.modelToDto(region);
    }

    /**
     * @return 
     */
    @Override
    public List<RegionDto> getAllRegion() {
        List<Region> regionList = regionRepository.findAll();
        return RegionMapper.RMP.listModelToDto(regionList);
    }

    /**
     * @param regionDto 
     * @return
     */
    @Override
    public RegionDto createOneRegion(RegionDto regionDto) {

        Region region = regionRepository.save(RegionMapper.RMP.dtoToModel(regionDto));
        return RegionMapper.RMP.modelToDto(region);
    }

    /**
     * @param regionId 
     * @param regionDto
     * @return
     */
    @Override
    public RegionDto updateOneRegion(Long regionId, RegionDto regionDto) {
        return null;
    }

    /**
     * @param regionId 
     */
    @Override
    public void deleteOneRegion(Long regionId) {

    }
}
