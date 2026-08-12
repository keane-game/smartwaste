package sn.smartwaste.collect.territory.application.service.impl;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.territory.application.mapper.GeometryMapper;
import sn.smartwaste.collect.territory.domain.repository.GeometryRepository;
import sn.smartwaste.collect.territory.domain.repository.RegionRepository;
import sn.smartwaste.collect.territory.application.dto.Region;
import sn.smartwaste.collect.territory.application.mapper.RegionMapper;
import sn.smartwaste.collect.territory.application.service.RegionService;

import java.util.List;

// P1-2 : RegionMapper.asDto lit l'association lazy `departments` — sans transaction explicite,
// ça ne fonctionnait que grâce à `spring.jpa.open-in-view` (défaut Spring Boot). Même patron
// que VehicleServiceImpl : classe en écriture par défaut, lectures passées en readOnly.
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;
    private final GeometryRepository geometryRepository;


    @Override
    @Transactional(readOnly = true)
    public Region readRegion(UUID regionId) {
        var region  = regionRepository.findById(regionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found ".formatted(regionId)
                ));
        return RegionMapper.RMP.asDto(region);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Region> readAllRegion() {
        var regionList = regionRepository.findByDeletionStatus(sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE);
        return RegionMapper.RMP.asListDto(regionList);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Region> readAllRegion(Pageable pageable) {
        return regionRepository.findByDeletionStatus(
                sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE, pageable).map(RegionMapper.RMP::asDto);
    }


    @Override
    public Region createRegion(Region region) {

        var savedRegion = regionRepository.save(RegionMapper.RMP.asModel(region));
        return RegionMapper.RMP.asDto(savedRegion);
    }


    /**
     * Corrige un défaut relevé par audit (2026-08-10, `docs/FRONTEND_API_MAPPING.md`) :
     * cette méthode était un stub (`return null`) — présente dans l'interface et l'implémentation,
     * jamais exposée par {@code RegionController}, jamais réellement écrite.
     */
    @Override
    public Region updateRegion(UUID regionId, Region region) {
        var existedRegion = regionRepository.findById(regionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Region with id [%s] not found to update ".formatted(regionId)
                ));
        if (region.getName() != null) {
            existedRegion.setName(region.getName());
        }
        if (region.getCode() != null) {
            existedRegion.setCode(region.getCode());
        }
        return RegionMapper.RMP.asDto(regionRepository.save(existedRegion));
    }


    /** Même défaut que {@link #updateRegion} : corps vide, jamais exposé. */
    @Override
    public void deleteRegion(UUID regionId) {
        var region = regionRepository.findById(regionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Region with id [%s] not found to delete".formatted(regionId)
                ));
        region.markForDeletion(java.time.LocalDateTime.now());
        regionRepository.save(region);
    }
}
