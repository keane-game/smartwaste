package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.Depotoir;

import java.util.List;

public interface DepotoirService {
    Depotoir getOneDepotoir(Long depotoirId);

    List<Depotoir> getAllDepotoir();

    Depotoir createOneDepotoir(Depotoir depotoir);

    Depotoir updateOneDepotoir(Long depotoirId, Depotoir depotoir);


    void deleteOneDepotoir(Long depotoirId);
}
