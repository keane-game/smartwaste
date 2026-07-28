package sn.smartwaste.collect.administration.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import sn.smartwaste.collect.administration.application.service.UploadFileService;

/**
 * Import de données de référence par fichier, ressource par ressource.
 *
 * <p>Ces endpoints vivaient dans {@code DashboardController} (contexte « Supervision »), ce qui
 * était doublement faux : un import n'est pas de la supervision, et il en héritait le préfixe
 * {@code /data}, ouvert en lecture publique — des <b>écritures en base</b> étaient donc joignables
 * sans jeton. La règle de sécurité a été restreinte au {@code GET} ; le déplacement ici corrige la
 * cause.
 *
 * <p>Les chemins sont <b>volontairement inchangés</b> : l'écran d'import du frontend appelle
 * {@code POST /data/{clé}} et ne doit pas être cassé par un déplacement de packages.
 */
@RestController
@RequestMapping("/data")
@RequiredArgsConstructor
public class DataImportController {

    private final UploadFileService uploadFileService;

    @PostMapping(value = "/commune" , consumes = "multipart/form-data")
    public String uploadDataCommune(@RequestParam("file") MultipartFile file) {
        return uploadFileService.uploadDataCommune (file);
    }

    @PostMapping(value = "/department" , consumes = "multipart/form-data")
    public String uploadDataDepartment(@RequestParam("file") MultipartFile file) {
       return uploadFileService.uploadDataDepartment (file);
    }

    @PostMapping(value = "/quartier" , consumes = "multipart/form-data")
    public String uploadDataQuartier(@RequestParam("file") MultipartFile file) {
       System.out.println ( file.getName ());
        return uploadFileService.uploadDataQuartier (file);
    }

    @PostMapping(value = "/circuitcollect" , consumes = "multipart/form-data")
    public String uploadDataCircuitCollect(@RequestParam("file") MultipartFile file) {
        return uploadFileService.uploadDataCircuitCollect (file);
    }

    @PostMapping(value = "/circuitbalayage" , consumes = "multipart/form-data")
    public String uploadDataCircuitBalayage(@RequestParam("file") MultipartFile file) {
        return uploadFileService.uploadDataCircuitBalayage (file);
    }

    @PostMapping(value = "/depotoir" , consumes = "multipart/form-data")
    public String uploadDataDepotoir(@RequestParam("file") MultipartFile file) {
        return uploadFileService.uploadDataDepotoir (file);
    }
}
