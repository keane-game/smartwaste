package sn.smartwaste.collect.territory.application.service.impl;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.territory.application.mapper.CommuneMapper;
import sn.smartwaste.collect.territory.application.mapper.RegionMapper;
import sn.smartwaste.collect.territory.domain.model.QuartierEntity;
import sn.smartwaste.collect.territory.domain.repository.CommuneRepository;
import sn.smartwaste.collect.territory.domain.repository.QuartierRepository;
import sn.smartwaste.collect.territory.application.dto.Quartier;
import sn.smartwaste.collect.territory.application.mapper.QuartierMapper;
import sn.smartwaste.collect.territory.application.service.QuartierService;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class QuartierServiceImpl implements QuartierService {
    private final CommuneRepository communeRepository;

    private final QuartierRepository quartierRepository;


    @Override
    public Quartier readQuartier(UUID quartierId) {
        var quartier = quartierRepository.findById(quartierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quartier with id [%s] not found ".formatted(quartierId)
                ));
        return QuartierMapper.QMP.asDto(quartier);
    }


    @Override
    public List<Quartier> readAllQuartier() {
        var quartierList = quartierRepository.findByDeletionStatus(sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE);
        return QuartierMapper.QMP.listModelToDto(quartierList);
    }

    @Override
    public Page<Quartier> readAllQuartier(Pageable pageable) {
        return quartierRepository.findByDeletionStatus (sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE, pageable).map (QuartierMapper.QMP::asDto);
    }

    @Override
    public Quartier createQuartier(Quartier quartier) {

        var communeId = quartier.getCommune().getCommuneId ();
        if(communeId != null) {
            var commune = communeRepository.findById (communeId).orElseThrow (
                    () -> new ResourceNotFoundException ("")
            );
           quartier.setCommune (commune);
        }
        var quartierSave = quartierRepository.save(QuartierMapper.QMP.asModel(quartier));
        return QuartierMapper.QMP.asDto(quartierSave);
    }


    @Override
    public Quartier updateQuartier(UUID quartierId, Quartier quartier) {
        var existedQuartier = quartierRepository.findById(quartierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quartier with id [%s] not found to update ".formatted(quartierId)
                ));
        if (!Objects.equals(existedQuartier.getQuartierId(), quartier.getQuartierId ())) {
            throw new ResourceNotFoundException(
                    "Corrupted body request or route");
        }
        if(quartier.getName() != null){
            existedQuartier.setName(quartier.getName());
        }
        if(quartier.getArea() != null){
            existedQuartier.setArea(quartier.getArea());
        }
        if(quartier.getCav() != null){
            existedQuartier.setCav(quartier.getCav());
        }
        if(quartier.getCCrca() != null){
            existedQuartier.setCCrca(quartier.getCCrca());
        }
        if(quartier.getLength() != null){
            existedQuartier.setLength(quartier.getLength());
        }
        if(quartier.getNumerozr() != null){
            existedQuartier.setNumerozr(quartier.getNumerozr());
        }
        if(quartier.getPoucentage() != null){
            existedQuartier.setPoucentage(quartier.getPoucentage());
        }
        if(quartier.getCodeCav() != null){
            existedQuartier.setCodeCav(quartier.getCodeCav());
        }
        if(quartier.getCodeCcrca() != null){
            existedQuartier.setCodeCcrca(quartier.getCodeCcrca());
        }
        if(quartier.getCodeEntity() != null){
            existedQuartier.setCodeEntity(quartier.getCodeEntity());
        }
        if(quartier.getCodeSzr() != null){
            existedQuartier.setCodeSzr(quartier.getCodeSzr());
        }
        if(quartier.getZoneCoron() != null){
            existedQuartier.setZoneCoron(quartier.getZoneCoron());
        }
        if(quartier.getCode() != null){
            existedQuartier.setCode(quartier.getCode());
        }
        if(quartier.getArea() != null){
            existedQuartier.setArea(quartier.getArea());
        }

        var quartierUpdate = quartierRepository.save(existedQuartier);
        return QuartierMapper.QMP.asDto(quartierUpdate);
    }


    @Override
    public void deleteQuartier(UUID quartierId) {
        var quartier = quartierRepository.findById(quartierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quartier with id [%s] not found ".formatted(quartierId)
                ));
        quartier.markForDeletion(java.time.LocalDateTime.now()); quartierRepository.save(quartier); // soft-delete (rétention + purge planifiée)
    }


}
