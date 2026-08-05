package sn.smartwaste.collect.waste.application.service;

import sn.smartwaste.collect.waste.application.dto.Circuit;

import java.util.List;

public interface CircuitService {

    Circuit readCircuit(Long circuitId);

    List<Circuit> readAllCircuit();

    Circuit createCircuit(Circuit circuit);

    Circuit updateCircuit(Long circuitId, Circuit circuitDto);

    void deleteCircuit(Long circuitId);
}
