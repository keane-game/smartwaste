package sn.smartwaste.collect.waste.application.service;

import org.springframework.web.multipart.MultipartFile;
import sn.smartwaste.collect.waste.application.dto.Image;

import java.io.IOException;

/**
 * Stockage des images hors base (P1-3 / ADR-0005).
 *
 * <p>Le fichier est écrit sur un stockage fichier/objet ; seule une <b>référence</b>
 * (chemin, URL publique, taille, nom, type MIME) est conservée en base via {@link Image}.
 */
public interface ImageService {

    /**
     * Persiste le fichier téléversé hors base et renvoie sa référence.
     *
     * @param file fichier téléversé
     * @return la référence à stocker en base, ou {@code null} si {@code file} est vide/absent
     */
    Image store(MultipartFile file) throws IOException;
}
