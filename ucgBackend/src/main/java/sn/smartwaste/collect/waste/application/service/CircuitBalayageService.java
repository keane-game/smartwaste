package sn.smartwaste.collect.waste.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.smartwaste.collect.waste.application.dto.CircuitBalayage;

import java.util.List;

public interface CircuitBalayageService {
    CircuitBalayage readCircuitBalayage(Long circuitBalayageId);

    List<CircuitBalayage> readAllCircuitBalayage();

    CircuitBalayage createCircuitBalayage(CircuitBalayage circuitBalayageDto);

    CircuitBalayage updateCircuitBalayage(Long circuitBalayageId, CircuitBalayage circuitBalayage);

    void deleteCircuitBalayage(Long circuitBalayageId);

    Page<CircuitBalayage> readAllCircuitBalayage(Pageable pageable);
}
