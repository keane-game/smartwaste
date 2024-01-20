package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.repository.CircuitBalayageRepository;
import sonaged.collecte.master.dto.CircuitBalayageDto;
import sonaged.collecte.master.mapper.CircuitBalayageMapper;
import sonaged.collecte.master.model.CircuitBalayage;
import sonaged.collecte.master.service.CircuitBalayageService;

import java.util.List;
@RequiredArgsConstructor
@Service
public class CircuitBalayageServiceImpl implements CircuitBalayageService {

    private final CircuitBalayageRepository circuitBalayageRepository;
    /**
     * @param circuitBalayageId 
     * @return
     */
    @Override
    public CircuitBalayageDto getOneCircuitBalayage(Long circuitBalayageId) {
        CircuitBalayage circuitBalayage = circuitBalayageRepository.findById(circuitBalayageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircuitCollect with id [%s] not found ".formatted(circuitBalayageId)
                ));
        return CircuitBalayageMapper.CBMP.modelToDto(circuitBalayage);
    }

    /**
     * @return 
     */
    @Override
    public List<CircuitBalayageDto> getAllCircuitBalayage() {
        List<CircuitBalayage> circuitBalayageList = circuitBalayageRepository.findAll();
        return CircuitBalayageMapper.CBMP.listModelToDto(circuitBalayageList);
    }

    /**
     * @param circuitBalayageDto 
     * @return
     */
    @Override
    public CircuitBalayageDto createOneCircuitBalayage(CircuitBalayageDto circuitBalayageDto) {
        CircuitBalayage circuitBalayage = CircuitBalayage.builder()
                .circuitbalayageCode(circuitBalayageDto.getCircuitbalayageCode())
                .circuitbalayageName(circuitBalayageDto.getCircuitbalayageName())
                .circuitbalayageShift(circuitBalayageDto.getCircuitbalayageShift())
                .circuitbalayageLength(circuitBalayageDto.getCircuitbalayageLength())
                .build();

        return CircuitBalayageMapper.CBMP.modelToDto(circuitBalayageRepository.save(circuitBalayage));
    }

    /**
     * @param circuitBalayageId
     * @param circuitBalayageDto
     * @return
     */
    @Override
    public CircuitBalayageDto updateOneCircuitBalayage(Long circuitBalayageId, CircuitBalayageDto circuitBalayageDto) {
        CircuitBalayage existedCircuitBalayage = circuitBalayageRepository.findById(circuitBalayageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircuitBalayage with id [%s] not found to update ".formatted(circuitBalayageId)
                ));
        if(circuitBalayageDto.getCircuitbalayageCode() != null ){
            existedCircuitBalayage.setCircuitbalayageCode(circuitBalayageDto.getCircuitbalayageCode());
        }
        if(circuitBalayageDto.getCircuitbalayageName() != null ){
            existedCircuitBalayage.setCircuitbalayageName(circuitBalayageDto.getCircuitbalayageName());
        }
        if(circuitBalayageDto.getCircuitbalayageShift() != null ){
            existedCircuitBalayage.setCircuitbalayageShift(circuitBalayageDto.getCircuitbalayageShift());
        }
        if(circuitBalayageDto.getCircuitbalayageLength() != null ){
            existedCircuitBalayage.setCircuitbalayageLength(circuitBalayageDto.getCircuitbalayageLength());
        }
        return CircuitBalayageMapper.CBMP.modelToDto(circuitBalayageRepository.save(existedCircuitBalayage));
    }

    /**
     * @param circuitBalayageId 
     */
    @Override
    public void deleteOneCircuitBalayage(Long circuitBalayageId) {
        CircuitBalayage circuitBalayage = circuitBalayageRepository.findById(circuitBalayageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircuitBalayage with id [%s] not found ".formatted(circuitBalayageId)
                ));
        circuitBalayageRepository.delete(circuitBalayage);
    }
}
