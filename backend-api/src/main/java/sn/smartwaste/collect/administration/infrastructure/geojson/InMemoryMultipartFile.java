package sn.smartwaste.collect.administration.infrastructure.geojson;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * {@link MultipartFile} en mémoire, adossé à un tableau d'octets.
 *
 * <p>Permet d'alimenter les méthodes {@code uploadDataXxx(MultipartFile)} existantes
 * (P1-5) avec le contenu d'un fichier GeoJSON chargé depuis le classpath / le disque,
 * sans dupliquer la logique de parsing.
 */
public class InMemoryMultipartFile implements MultipartFile {

    private final String name;
    private final String contentType;
    private final byte[] content;

    public InMemoryMultipartFile(String name, String contentType, byte[] content) {
        this.name = name;
        this.contentType = contentType;
        this.content = content != null ? content : new byte[0];
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return name;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() {
        return content;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        Files.write(dest.toPath(), content);
    }
}
