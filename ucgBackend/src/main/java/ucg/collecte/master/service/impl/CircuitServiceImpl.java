package ucg.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ucg.collecte.master.dto.CircuitDto;
import ucg.collecte.master.exception.ResourceNotFoundException;
import ucg.collecte.master.mapper.CircuitMapper;
import ucg.collecte.master.model.Circuit;
import ucg.collecte.master.repository.CircuitRepository;
import ucg.collecte.master.service.CircuitService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CircuitServiceImpl implements CircuitService {

    private final CircuitRepository circuitRepository;

    /**
     * @param circuitId 
     * @return
     */
    @Override
    public CircuitDto getOneCircuit(Long circuitId) {
        Circuit circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found ".formatted(circuitId)
                ));
        return CircuitMapper.CIMP.modelToDto(circuit);
    }

    /**
     * @return 
     */
    @Override
    public List<CircuitDto> getAllCircuit() {
        List<Circuit> circuitList = circuitRepository.findAll();
        return CircuitMapper.CIMP.listModelToDto(circuitList);
    }

    /**
     * @param circuitDto 
     * @return
     */
    @Override
    public CircuitDto createOneCircuit(CircuitDto circuitDto) {
        Circuit circuit = Circuit.builder()
                .circuitCode(circuitDto.getCircuitCode())
                .circuitName(circuitDto.getCircuitName())
                .build();
        Circuit savedCircuit = circuitRepository.save(circuit);
        return CircuitMapper.CIMP.modelToDto(savedCircuit);
    }

    /**
     * @param circuitId
     * @param circuitDto
     * @return
     */
    @Override
    public CircuitDto updateOneCircuit(Long circuitId, CircuitDto circuitDto) {
        Circuit existedCircuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Circuit with id [%s] not found to update ".formatted(circuitId)
                ));
        if (circuitDto.getCircuitCode() != null){
            existedCircuit.setCircuitCode(circuitDto.getCircuitCode());
        }
        if (circuitDto.getCircuitName() != null){
            existedCircuit.setCircuitName(circuitDto.getCircuitName());
        }
        Circuit updatedCircuit =  circuitRepository.save(existedCircuit);
        return CircuitMapper.CIMP.modelToDto(updatedCircuit);
    }

    /**
     * @param circuitId 
     */
    @Override
    public void deleteOneCircuit(Long circuitId) {
        Circuit circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found to delete".formatted(circuitId)
                ));
        circuitRepository.delete(circuit);
    }
}
