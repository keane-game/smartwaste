package sn.smartwaste.collect.waste.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.smartwaste.collect.waste.application.dto.CircuitBalayage;

import java.util.List;
import java.util.UUID;

public interface CircuitBalayageService {
    CircuitBalayage readCircuitBalayage(UUID circuitBalayageId);

    List<CircuitBalayage> readAllCircuitBalayage();

    CircuitBalayage createCircuitBalayage(CircuitBalayage circuitBalayageDto);

    CircuitBalayage updateCircuitBalayage(UUID circuitBalayageId, CircuitBalayage circuitBalayage);

    void deleteCircuitBalayage(UUID circuitBalayageId);

    Page<CircuitBalayage> readAllCircuitBalayage(Pageable pageable);
}
