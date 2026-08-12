package sn.smartwaste.collect.waste.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.smartwaste.collect.waste.application.dto.Circuit;

import java.util.List;
import java.util.UUID;

public interface CircuitService {

    Circuit readCircuit(UUID circuitId);

    List<Circuit> readAllCircuit();

    /** Corrige un gap relevé par audit (2026-08-10) : ressource sans pagination. */
    Page<Circuit> readAllCircuit(Pageable pageable);

    Circuit createCircuit(Circuit circuit);

    Circuit updateCircuit(UUID circuitId, Circuit circuitDto);

    void deleteCircuit(UUID circuitId);
}
