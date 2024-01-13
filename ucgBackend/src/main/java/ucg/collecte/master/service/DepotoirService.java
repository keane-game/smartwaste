package ucg.collecte.master.service;

import ucg.collecte.master.dto.DepotDto;
import ucg.collecte.master.model.Depot;

import java.util.List;

public interface DepotService {
    DepotDto getOneDepot(Long depotId);

    List<DepotDto> getAllDepot();

    DepotDto createOneDepot(DepotDto depotDto);

    DepotDto updateOneDepot(Long depotId, DepotDto depotDto);


    void deleteOneDepot(Long depotId);
}
