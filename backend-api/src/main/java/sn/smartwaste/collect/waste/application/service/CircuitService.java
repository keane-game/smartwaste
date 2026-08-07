package sn.smartwaste.collect.waste.application.service;

import sn.smartwaste.collect.waste.application.dto.Circuit;

import java.util.List;
import java.util.UUID;

public interface CircuitService {

    Circuit readCircuit(UUID circuitId);

    List<Circuit> readAllCircuit();

    Circuit createCircuit(Circuit circuit);

    Circuit updateCircuit(UUID circuitId, Circuit circuitDto);

    void deleteCircuit(UUID circuitId);
}
