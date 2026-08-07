package sn.smartwaste.collect.waste.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.smartwaste.collect.waste.application.dto.CircuitCollect;

import java.util.List;
import java.util.UUID;

public interface CircuitCollectService {
    CircuitCollect readCircuitCollect(UUID circuitCollectId);

    List<CircuitCollect> readAllCircuitCollect();

    CircuitCollect createCircuitCollect(CircuitCollect circuitCollect);

    CircuitCollect updateCircuitCollect(UUID circuitCollectId, CircuitCollect circuitCollectDto);

    void deleteCircuitCollect(UUID circuitCollectId);

    Page<CircuitCollect> readAllCircuitCollect(Pageable pageable);
}
