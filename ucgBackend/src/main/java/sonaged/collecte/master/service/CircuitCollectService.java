package sonaged.collecte.master.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sonaged.collecte.master.dto.CircuitCollect;

import java.util.List;

public interface CircuitCollectService {
    CircuitCollect readCircuitCollect(Long circuitCollectId);

    List<CircuitCollect> readAllCircuitCollect();

    CircuitCollect createCircuitCollect(CircuitCollect circuitCollect);

    CircuitCollect updateCircuitCollect(Long circuitCollectId, CircuitCollect circuitCollectDto);

    void deleteCircuitCollect(Long circuitCollectId);

    Page<CircuitCollect> readAllCircuitCollect(Pageable pageable);
}
