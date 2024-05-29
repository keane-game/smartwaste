package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.Circuit;

import java.util.List;

public interface CircuitService {

    Circuit getOneCircuit(Long circuitId);

    List<Circuit> getAllCircuit();

    Circuit createOneCircuit(Circuit circuit);

    Circuit updateOneCircuit(Long circuitId, Circuit circuitDto);
    void deleteOneCircuit(Long circuitId);
}
