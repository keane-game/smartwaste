package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.CircuitCollect;

import java.util.List;

public interface CircuitCollectService {
    CircuitCollect getOneCircuitCollect(Long circuitCollectId);

    List<CircuitCollect> getAllCircuitCollect();

    CircuitCollect createOneCircuitCollect(CircuitCollect circuitCollect);

    CircuitCollect updateOneCircuitCollect(Long circuitCollectId, CircuitCollect circuitCollectDto);
    void deleteOneCircuitCollect(Long circuitCollectId);
}
