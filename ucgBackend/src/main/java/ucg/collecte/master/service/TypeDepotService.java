package ucg.collecte.master.service;

import ucg.collecte.master.model.TypeDepot;

import java.util.List;

public interface TypeDepotService {
    TypeDepot getOneTypeDepot(Long typeDepotId);

    List<TypeDepot> getAllTypeDepot();

    TypeDepot createOneTypeDepot(TypeDepot typeDepot);

    TypeDepot updateOneTypeDepot(Long typeDepotId, TypeDepot typeDepot);
    void deleteOneTypeDepot(Long typeDepotId);
}
