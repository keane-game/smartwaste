package sn.smartwaste.collect.waste.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.smartwaste.collect.waste.domain.model.CircuitShift;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.waste.domain.repository.CircuitBalayageRepository;
import sn.smartwaste.collect.waste.application.dto.CircuitBalayage;
import sn.smartwaste.collect.waste.application.mapper.CircuitBalayageMapper;
import sn.smartwaste.collect.waste.domain.model.CircuitBalayageEntity;
import sn.smartwaste.collect.waste.application.service.CircuitBalayageService;
import sn.smartwaste.collect.tenant.application.api.CurrentTenantProvider;

import java.util.List;
import java.util.UUID;

// P1-2 : CircuitBalayageMapper.asDto lit l'association lazy `geometry` — même correctif que
// DepotoirServiceImpl.
@RequiredArgsConstructor
@Service
@Transactional
public class CircuitBalayageServiceImpl implements CircuitBalayageService {

    private final CircuitBalayageRepository circuitBalayageRepository;
    private final CurrentTenantProvider currentTenantProvider;

    @Override
    @Transactional(readOnly = true)
    public CircuitBalayage readCircuitBalayage(UUID circuitBalayageId) {
        var circuitBalayage = circuitBalayageRepository.findById(circuitBalayageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircuitCollect with id [%s] not found ".formatted(circuitBalayageId)
                ));
        return CircuitBalayageMapper.CBMP.asDto(circuitBalayage);
    }


    @Override
    @Transactional(readOnly = true)
    public List<CircuitBalayage> readAllCircuitBalayage() {
        var circuitBalayageList = circuitBalayageRepository.findByDeletionStatus(sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE);
        return CircuitBalayageMapper.CBMP.asListDto(circuitBalayageList);
    }

    @Transactional(readOnly = true)
    public Page<CircuitBalayage> readAllCircuitBalayage(Pageable pageable){
        return circuitBalayageRepository.findByDeletionStatus (sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE, pageable).map(CircuitBalayageMapper.CBMP::asDto);
    }

    @Override
    public CircuitBalayage createCircuitBalayage(CircuitBalayage circuitBalayage) {
        var toCreate = CircuitBalayageMapper.CBMP.asModel(circuitBalayage);
        // ADR-0020 : jamais depuis le DTO client.
        toCreate.setOrganizationId(currentTenantProvider.currentOrganizationId()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucune collectivité rattachée au compte courant : impossible de créer un circuit de balayage")));
        var savedCircuitBalayage = circuitBalayageRepository.save(toCreate);
        return CircuitBalayageMapper.CBMP.asDto(savedCircuitBalayage);
    }


    @Override
    public CircuitBalayage updateCircuitBalayage(UUID circuitBalayageId, CircuitBalayage circuitBalayage) {
        var existedCircuitBalayage = circuitBalayageRepository.findById(circuitBalayageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircuitBalayage with id [%s] not found to update ".formatted(circuitBalayageId)
                ));
        if(circuitBalayage.getCode() != null ){
            existedCircuitBalayage.setCode(circuitBalayage.getCode());
        }
        if(circuitBalayage.getName() != null ){
            existedCircuitBalayage.setName(circuitBalayage.getName());
        }
        if(circuitBalayage.getShift() != null ){
            existedCircuitBalayage.setShift(CircuitShift.valueOf (circuitBalayage.getShift()));
        }
        if(circuitBalayage.getLength() != null ){
            existedCircuitBalayage.setLength(circuitBalayage.getLength());
        }
        return CircuitBalayageMapper.CBMP.asDto(circuitBalayageRepository.save(existedCircuitBalayage));
    }


    @Override
    public void deleteCircuitBalayage(UUID circuitBalayageId) {
        var circuitBalayage = circuitBalayageRepository.findById(circuitBalayageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircuitBalayage with id [%s] not found ".formatted(circuitBalayageId)
                ));
        circuitBalayage.markForDeletion(java.time.LocalDateTime.now()); circuitBalayageRepository.save(circuitBalayage); // soft-delete (rétention + purge planifiée)
    }
}
