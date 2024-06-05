package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.Depotoir;

import java.util.List;

public interface DepotoirService {
    Depotoir readDepotoir(Long depotoirId);

    List<Depotoir> readAllDepotoir();

    Depotoir createDepotoir(Depotoir depotoir);

    Depotoir updateDepotoir(Long depotoirId, Depotoir depotoir);


    void deleteDepotoir(Long depotoirId);
}
