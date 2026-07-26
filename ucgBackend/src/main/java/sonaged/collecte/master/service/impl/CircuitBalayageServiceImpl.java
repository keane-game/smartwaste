package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.enums.CircuitShift;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.repository.CircuitBalayageRepository;
import sonaged.collecte.master.dto.CircuitBalayage;
import sonaged.collecte.master.mapper.CircuitBalayageMapper;
import sonaged.collecte.master.model.CircuitBalayageEntity;
import sonaged.collecte.master.service.CircuitBalayageService;

import java.util.List;
@RequiredArgsConstructor
@Service
public class CircuitBalayageServiceImpl implements CircuitBalayageService {

    private final CircuitBalayageRepository circuitBalayageRepository;

    @Override
    public CircuitBalayage readCircuitBalayage(Long circuitBalayageId) {
        var circuitBalayage = circuitBalayageRepository.findById(circuitBalayageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircuitCollect with id [%s] not found ".formatted(circuitBalayageId)
                ));
        return CircuitBalayageMapper.CBMP.asDto(circuitBalayage);
    }


    @Override
    public List<CircuitBalayage> readAllCircuitBalayage() {
        var circuitBalayageList = circuitBalayageRepository.findByDeletionStatus(sonaged.collecte.master.enums.DeletionStatus.ACTIVE);
        return CircuitBalayageMapper.CBMP.asListDto(circuitBalayageList);
    }

    public Page<CircuitBalayage> readAllCircuitBalayage(Pageable pageable){
        return circuitBalayageRepository.findByDeletionStatus (sonaged.collecte.master.enums.DeletionStatus.ACTIVE, pageable).map(CircuitBalayageMapper.CBMP::asDto);
    }

    @Override
    public CircuitBalayage createCircuitBalayage(CircuitBalayage circuitBalayage) {
        var savedCircuitBalayage = circuitBalayageRepository.save(CircuitBalayageMapper.CBMP.asModel (circuitBalayage));
        return CircuitBalayageMapper.CBMP.asDto(savedCircuitBalayage);
    }


    @Override
    public CircuitBalayage updateCircuitBalayage(Long circuitBalayageId, CircuitBalayage circuitBalayage) {
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
    public void deleteCircuitBalayage(Long circuitBalayageId) {
        var circuitBalayage = circuitBalayageRepository.findById(circuitBalayageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircuitBalayage with id [%s] not found ".formatted(circuitBalayageId)
                ));
        circuitBalayage.markForDeletion(java.time.LocalDateTime.now()); circuitBalayageRepository.save(circuitBalayage); // soft-delete (rétention + purge planifiée)
    }
}
