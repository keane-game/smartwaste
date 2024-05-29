package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.CircuitBalayage;

import java.util.List;

public interface CircuitBalayageService {
    CircuitBalayage getOneCircuitBalayage(Long circuitBalayageId);

    List<CircuitBalayage> getAllCircuitBalayage();

    CircuitBalayage createOneCircuitBalayage(CircuitBalayage circuitBalayageDto);

    CircuitBalayage updateOneCircuitBalayage(Long circuitBalayageId, CircuitBalayage circuitBalayage);
    void deleteOneCircuitBalayage(Long circuitBalayageId);
}
