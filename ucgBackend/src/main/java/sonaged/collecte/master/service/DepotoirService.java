package sonaged.collecte.master.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sonaged.collecte.master.dto.Depotoir;
import sonaged.collecte.master.dto.DepotoirMaps;

import java.util.List;

public interface DepotoirService {
    Depotoir readDepotoir(Long depotoirId);

    List<Depotoir> readAllDepotoir();

    Depotoir createDepotoir(Depotoir depotoir);

    Depotoir updateDepotoir(Long depotoirId, Depotoir depotoir);

    void deleteDepotoir(Long depotoirId);

    Page<Depotoir> readAllDepotoir(Pageable pageable);

    List<DepotoirMaps> getDepotoirMap();
}
