package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.TypeDepotoir;

import java.util.List;

public interface TypeDepotoirService {

    TypeDepotoir readTypeDepotoir(Long typeDepotoirId);

    List<TypeDepotoir> readAllTypeDepotoir();

    TypeDepotoir createTypeDepotoir(TypeDepotoir typeDepotoir);

    TypeDepotoir updateTypeDepotoir(Long typeDepotoirId, TypeDepotoir typeDepotoir);

    void deleteTypeDepotoir(Long typeDepotoirId);
}
