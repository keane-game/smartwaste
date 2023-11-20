package ucg.collecte.master.service;

import ucg.collecte.master.dto.CircuitDto;

import java.util.List;

public interface CircuitService {

    CircuitDto getOneCircuit(Long circuitId);

    List<CircuitDto> getAllCircuit();

    CircuitDto createOneCircuit(CircuitDto circuitDto);

    CircuitDto updateOneCircuit(Long circuitId, CircuitDto circuitDto);
    void deleteOneCircuit(Long circuitId);
}
