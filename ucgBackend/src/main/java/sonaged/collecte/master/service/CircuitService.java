package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.Circuit;

import java.util.List;

public interface CircuitService {

    Circuit readCircuit(Long circuitId);

    List<Circuit> readAllCircuit();

    Circuit createCircuit(Circuit circuit);

    Circuit updateCircuit(Long circuitId, Circuit circuitDto);

    void deleteCircuit(Long circuitId);
}
