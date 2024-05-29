package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.TypeDepotoir;

import java.util.List;

public interface TypeDepotoirService {
    TypeDepotoir getOneTypeDepotoir(Long typeDepotoirId);

    List<TypeDepotoir> getAllTypeDepotoir();

    TypeDepotoir createOneTypeDepotoir(TypeDepotoir typeDepotoir);

    TypeDepotoir updateOneTypeDepotoir(Long typeDepotoirId, TypeDepotoir typeDepotoir);

    void deleteOneTypeDepotoir(Long typeDepotoirId);
}
