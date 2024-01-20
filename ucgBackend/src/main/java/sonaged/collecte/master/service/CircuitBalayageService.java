package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.CircuitBalayageDto;

import java.util.List;

public interface CircuitBalayageService {
    CircuitBalayageDto getOneCircuitBalayage(Long circuitBalayageId);

    List<CircuitBalayageDto> getAllCircuitBalayage();

    CircuitBalayageDto createOneCircuitBalayage(CircuitBalayageDto circuitBalayageDto);

    CircuitBalayageDto updateOneCircuitBalayage(Long circuitBalayageId, CircuitBalayageDto circuitBalayageDto);
    void deleteOneCircuitBalayage(Long circuitBalayageId);
}
