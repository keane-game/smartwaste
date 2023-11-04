package ucg.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ucg.collecte.master.dto.RegionDto;
import ucg.collecte.master.exception.ResourceNotFoundException;
import ucg.collecte.master.mapper.RegionMapper;
import ucg.collecte.master.mapper.UserMapper;
import ucg.collecte.master.model.Region;
import ucg.collecte.master.model.User;
import ucg.collecte.master.repository.RegionRepository;
import ucg.collecte.master.service.RegionService;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;

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
        Region region = Region.builder()
                .regionName(regionDto.getRegionName())
                .regionCode(regionDto.getRegionCode())
                .build();

        return RegionMapper
                .RMP
                .modelToDto(regionRepository
                        .save(region));
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
