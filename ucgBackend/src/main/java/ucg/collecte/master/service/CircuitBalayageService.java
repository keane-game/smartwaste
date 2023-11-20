package ucg.collecte.master.service;

import ucg.collecte.master.dto.CircuitBalayageDto;

import java.util.List;

public interface CircuitBalayageService {
    CircuitBalayageDto getOneCircuitBalayage(Long circuitBalayageId);

    List<CircuitBalayageDto> getAllCircuitBalayage();

    CircuitBalayageDto createOneCircuitBalayage(CircuitBalayageDto circuitBalayageDto);

    CircuitBalayageDto updateOneCircuitBalayage(Long circuitBalayageId, CircuitBalayageDto circuitBalayageDto);
    void deleteOneCircuitBalayage(Long circuitBalayageId);
}
