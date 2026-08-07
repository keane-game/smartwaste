package sn.smartwaste.collect.waste.application.service;

import sn.smartwaste.collect.waste.application.dto.TypeDepotoir;

import java.util.List;
import java.util.UUID;

public interface TypeDepotoirService {

    TypeDepotoir readTypeDepotoir(UUID typeDepotoirId);

    List<TypeDepotoir> readAllTypeDepotoir();

    TypeDepotoir createTypeDepotoir(TypeDepotoir typeDepotoir);

    TypeDepotoir updateTypeDepotoir(UUID typeDepotoirId, TypeDepotoir typeDepotoir);

    void deleteTypeDepotoir(UUID typeDepotoirId);
}
