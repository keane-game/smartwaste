package ucg.collecte.master.service;

import ucg.collecte.master.dto.TypeDepotDto;
import ucg.collecte.master.model.TypeDepot;

import java.util.List;

public interface TypeDepotService {
    TypeDepotDto getOneTypeDepot(Long typeDepotId);

    List<TypeDepotDto> getAllTypeDepot();

    TypeDepotDto createOneTypeDepot(TypeDepotDto typeDepot);

    TypeDepotDto updateOneTypeDepot(Long typeDepotId, TypeDepotDto typeDepot);


    void deleteOneTypeDepot(Long typeDepotId);
}
