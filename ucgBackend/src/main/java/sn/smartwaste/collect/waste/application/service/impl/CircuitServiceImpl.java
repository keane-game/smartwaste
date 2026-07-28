package sn.smartwaste.collect.waste.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.waste.domain.repository.CircuitRepository;
import sn.smartwaste.collect.waste.application.dto.Circuit;
import sn.smartwaste.collect.waste.application.mapper.CircuitMapper;
import sn.smartwaste.collect.waste.application.service.CircuitService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CircuitServiceImpl implements CircuitService {

    private final CircuitRepository circuitRepository;


    @Override
    public Circuit readCircuit(Long circuitId) {
        var circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found ".formatted(circuitId)
                ));
        return CircuitMapper.CIMP.asDto (circuit);
    }


    @Override
    public List<Circuit> readAllCircuit() {
        var circuitList = circuitRepository.findByDeletionStatus(sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE);
        return CircuitMapper.CIMP.asListDto (circuitList);
    }


    @Override
    public Circuit createCircuit(Circuit circuit) {
        var savedCircuit = circuitRepository.save(CircuitMapper.CIMP.asModel(circuit));
        return CircuitMapper.CIMP.asDto(savedCircuit);
    }


    @Override
    public Circuit updateCircuit(Long circuitId, Circuit circuit) {
        var existedCircuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Circuit with id [%s] not found to update ".formatted(circuitId)
                ));
        if (circuit.getCode() != null){
            existedCircuit.setCode(circuit.getCode());
        }
        if (circuit.getName() != null){
            existedCircuit.setName(circuit.getName());
        }

        return CircuitMapper.CIMP.asDto(circuitRepository.save(existedCircuit));
    }

    /**
     * @param circuitId 
     */
    @Override
    public void deleteCircuit(Long circuitId) {
        var circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found to delete".formatted(circuitId)
                ));
        circuit.markForDeletion(java.time.LocalDateTime.now()); circuitRepository.save(circuit); // soft-delete (rétention + purge planifiée)
    }
}
