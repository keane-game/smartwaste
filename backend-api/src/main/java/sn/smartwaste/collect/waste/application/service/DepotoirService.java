package sn.smartwaste.collect.waste.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.smartwaste.collect.waste.application.dto.Depotoir;
import sn.smartwaste.collect.waste.application.api.DepotoirMaps;

import java.util.List;
import java.util.UUID;

public interface DepotoirService {
    Depotoir readDepotoir(UUID depotoirId);

    List<Depotoir> readAllDepotoir();

    Depotoir createDepotoir(Depotoir depotoir);

    Depotoir updateDepotoir(UUID depotoirId, Depotoir depotoir);

    /** Suppression logique (soft-delete) : passe en attente de suppression (purge après rétention). */
    void deleteDepotoir(UUID depotoirId);

    /** Restaure un dépotoir en attente de suppression (si le délai de rétention n'est pas dépassé). */
    Depotoir restoreDepotoir(UUID depotoirId);

    /** Liste des dépotoirs en attente de suppression (avec date de purge prévue). */
    List<Depotoir> readPendingDeletions();

    Page<Depotoir> readAllDepotoir(Pageable pageable);

    List<DepotoirMaps> getDepotoirMap();
}
