package sn.smartwaste.collect.waste.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.smartwaste.collect.waste.application.dto.TypeDepotoir;

import java.util.List;
import java.util.UUID;

public interface TypeDepotoirService {

    TypeDepotoir readTypeDepotoir(UUID typeDepotoirId);

    List<TypeDepotoir> readAllTypeDepotoir();

    /** Corrige un gap relevé par audit (2026-08-10) : ressource sans pagination. */
    Page<TypeDepotoir> readAllTypeDepotoir(Pageable pageable);

    TypeDepotoir createTypeDepotoir(TypeDepotoir typeDepotoir);

    TypeDepotoir updateTypeDepotoir(UUID typeDepotoirId, TypeDepotoir typeDepotoir);

    void deleteTypeDepotoir(UUID typeDepotoirId);
}
