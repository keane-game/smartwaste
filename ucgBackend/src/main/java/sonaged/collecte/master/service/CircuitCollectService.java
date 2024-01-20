package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.CircuitCollectDto;

import java.util.List;

public interface CircuitCollectService {
    CircuitCollectDto getOneCircuitCollect(Long circuitCollectId);

    List<CircuitCollectDto> getAllCircuitCollect();

    CircuitCollectDto createOneCircuitCollect(CircuitCollectDto circuitCollectDto);

    CircuitCollectDto updateOneCircuitCollect(Long circuitCollectId, CircuitCollectDto circuitCollectDto);
    void deleteOneCircuitCollect(Long circuitCollectId);
}
