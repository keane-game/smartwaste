package sn.smartwaste.collect.waste.application.service;

import sn.smartwaste.collect.waste.application.dto.TypeDepotoir;

import java.util.List;

public interface TypeDepotoirService {

    TypeDepotoir readTypeDepotoir(Long typeDepotoirId);

    List<TypeDepotoir> readAllTypeDepotoir();

    TypeDepotoir createTypeDepotoir(TypeDepotoir typeDepotoir);

    TypeDepotoir updateTypeDepotoir(Long typeDepotoirId, TypeDepotoir typeDepotoir);

    void deleteTypeDepotoir(Long typeDepotoirId);
}
