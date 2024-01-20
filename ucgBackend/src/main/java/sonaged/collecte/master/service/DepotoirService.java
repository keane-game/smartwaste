package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.DepotoirDto;
import sonaged.collecte.master.model.Depotoir;

import java.util.List;

public interface DepotoirService {
    DepotoirDto getOneDepotoir(Long depotoirId);

    List<DepotoirDto> getAllDepotoir();

    DepotoirDto createOneDepotoir(DepotoirDto depotoirDto);

    DepotoirDto updateOneDepotoir(Long depotoirId, DepotoirDto depotoirDto);


    void deleteOneDepotoir(Long depotoirId);
}
