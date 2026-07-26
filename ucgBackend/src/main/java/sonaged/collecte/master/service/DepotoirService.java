package sonaged.collecte.master.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sonaged.collecte.master.dto.Depotoir;
import sonaged.collecte.master.dto.DepotoirMaps;

import java.util.List;

public interface DepotoirService {
    Depotoir readDepotoir(Long depotoirId);

    List<Depotoir> readAllDepotoir();

    Depotoir createDepotoir(Depotoir depotoir);

    Depotoir updateDepotoir(Long depotoirId, Depotoir depotoir);

    /** Suppression logique (soft-delete) : passe en attente de suppression (purge après rétention). */
    void deleteDepotoir(Long depotoirId);

    /** Restaure un dépotoir en attente de suppression (si le délai de rétention n'est pas dépassé). */
    Depotoir restoreDepotoir(Long depotoirId);

    /** Liste des dépotoirs en attente de suppression (avec date de purge prévue). */
    List<Depotoir> readPendingDeletions();

    Page<Depotoir> readAllDepotoir(Pageable pageable);

    List<DepotoirMaps> getDepotoirMap();
}
