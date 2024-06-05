package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.CircuitCollect;

import java.util.List;

public interface CircuitCollectService {
    CircuitCollect readCircuitCollect(Long circuitCollectId);

    List<CircuitCollect> readAllCircuitCollect();

    CircuitCollect createCircuitCollect(CircuitCollect circuitCollect);

    CircuitCollect updateCircuitCollect(Long circuitCollectId, CircuitCollect circuitCollectDto);

    void deleteCircuitCollect(Long circuitCollectId);
}
