package sn.smartwaste.collect.administration.application.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadFileService {

    String uploadDataDepartment(MultipartFile file);
    String uploadDataCommune(MultipartFile file);
    String uploadDataQuartier(MultipartFile file);
    String uploadDataCircuitCollect(MultipartFile file);
    String uploadDataCircuitBalayage(MultipartFile file);
    String uploadDataDepotoir(MultipartFile file);

    /**
     * Importe les points de collecte répartis sur <b>plusieurs</b> fichiers, en une seule passe.
     *
     * <p>Les 7 fichiers de points de {@code datas/} sont des exports successifs des mêmes couches
     * SIG : 132 entrées pour 71 coordonnées distinctes. Les traiter un par un les dupliquerait ; le
     * dédoublonnage n'a de sens qu'à l'échelle de l'ensemble, d'où cette méthode plutôt qu'une
     * boucle d'appels à {@link #uploadDataDepotoir(MultipartFile)}. Voir ADR-0015 §2-3.
     *
     * @return un rapport lisible : importés, doublons ignorés, points sans commune
     */
    String uploadDataDepotoirs(java.util.List<? extends MultipartFile> files);


}
