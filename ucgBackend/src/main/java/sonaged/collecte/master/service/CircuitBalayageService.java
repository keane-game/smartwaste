package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.CircuitBalayage;

import java.util.List;

public interface CircuitBalayageService {
    CircuitBalayage readCircuitBalayage(Long circuitBalayageId);

    List<CircuitBalayage> readAllCircuitBalayage();

    CircuitBalayage createCircuitBalayage(CircuitBalayage circuitBalayageDto);

    CircuitBalayage updateCircuitBalayage(Long circuitBalayageId, CircuitBalayage circuitBalayage);

    void deleteCircuitBalayage(Long circuitBalayageId);
}
