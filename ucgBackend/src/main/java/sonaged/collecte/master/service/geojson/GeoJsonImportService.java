package sonaged.collecte.master.service.geojson;

import java.util.Map;

/**
 * Import automatisé des GeoJSON de référence (P1-5).
 *
 * <p>Charge les fichiers {@code datas/*.json} (département, communes, quartiers, circuits,
 * dépotoirs) en base en réutilisant la logique de parsing existante
 * ({@link sonaged.collecte.master.service.UploadFileService}), dans l'ordre de dépendance
 * (département → commune → quartier → circuits → dépotoir).
 */
public interface GeoJsonImportService {

    /**
     * Importe tous les jeux de référence.
     *
     * @param force si {@code false}, chaque étape est ignorée quand l'entité correspondante
     *              est déjà peuplée (idempotence) ; si {@code true}, l'import est rejoué même
     *              si des données existent (attention aux doublons — pas d'upsert).
     * @return un récapitulatif ordonné {@code étape -> résultat}.
     */
    Map<String, String> importAll(boolean force);
}
