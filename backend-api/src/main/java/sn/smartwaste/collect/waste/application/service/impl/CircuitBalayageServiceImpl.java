package sn.smartwaste.collect.waste.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sn.smartwaste.collect.waste.domain.model.CircuitShift;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.waste.domain.repository.CircuitBalayageRepository;
import sn.smartwaste.collect.waste.application.dto.CircuitBalayage;
import sn.smartwaste.collect.waste.application.mapper.CircuitBalayageMapper;
import sn.smartwaste.collect.waste.domain.model.CircuitBalayageEntity;
import sn.smartwaste.collect.waste.application.service.CircuitBalayageService;

import java.util.List;
import java.util.UUID;
@RequiredArgsConstructor
@Service
public class CircuitBalayageServiceImpl implements CircuitBalayageService {

    private final CircuitBalayageRepository circuitBalayageRepository;

    @Override
    public CircuitBalayage readCircuitBalayage(UUID circuitBalayageId) {
        var circuitBalayage = circuitBalayageRepository.findById(circuitBalayageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircuitCollect with id [%s] not found ".formatted(circuitBalayageId)
                ));
        return CircuitBalayageMapper.CBMP.asDto(circuitBalayage);
    }


    @Override
    public List<CircuitBalayage> readAllCircuitBalayage() {
        var circuitBalayageList = circuitBalayageRepository.findByDeletionStatus(sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE);
        return CircuitBalayageMapper.CBMP.asListDto(circuitBalayageList);
    }

    public Page<CircuitBalayage> readAllCircuitBalayage(Pageable pageable){
        return circuitBalayageRepository.findByDeletionStatus (sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE, pageable).map(CircuitBalayageMapper.CBMP::asDto);
    }

    @Override
    public CircuitBalayage createCircuitBalayage(CircuitBalayage circuitBalayage) {
        var savedCircuitBalayage = circuitBalayageRepository.save(CircuitBalayageMapper.CBMP.asModel (circuitBalayage));
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
