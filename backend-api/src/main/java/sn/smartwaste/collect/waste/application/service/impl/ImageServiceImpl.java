package sn.smartwaste.collect.waste.application.service.impl;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import sn.smartwaste.collect.waste.application.dto.Image;
import sn.smartwaste.collect.waste.application.service.ImageService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

/**
 * Stockage des images/fichiers sur MinIO (objet, compatible S3) — P1-3 / ADR-0005.
 *
 * <p>Chaque fichier est envoyé sous une clé aléatoire dans le bucket
 * {@code sonaged.storage.minio.bucket} ; seule une {@link Image} portant une <b>référence</b>
 * (clé d'objet + URL publique + taille + nom + type MIME) est conservée en base. Aucun octet
 * n'est stocké dans la base de données.
 */
@Service
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final MinioClient minioClient;
    private final String bucket;
    private final String publicBaseUrl;

    public ImageServiceImpl(
            MinioClient minioClient,
            @Value("${sonaged.storage.minio.bucket:sonaged-images}") String bucket,
            @Value("${sonaged.storage.minio.endpoint:http://localhost:9000}") String endpoint,
            @Value("${sonaged.storage.minio.public-base-url:}") String publicBaseUrl) {
        this.minioClient = minioClient;
        this.bucket = bucket;
        // URL publique des objets : soit un préfixe explicite (proxy/CDN), soit endpoint/bucket.
        String base = (publicBaseUrl == null || publicBaseUrl.isBlank())
                ? stripTrailingSlash(endpoint) + "/" + bucket
                : stripTrailingSlash(publicBaseUrl);
        this.publicBaseUrl = base;
    }

    @Override
    public Image store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        ensureBucket();

        String original = StringUtils.cleanPath(
                Objects.requireNonNullElse(file.getOriginalFilename(), "image"));
        String extension = StringUtils.getFilenameExtension(original);
        String objectKey = UUID.randomUUID() + (extension == null || extension.isBlank() ? "" : "." + extension);
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(in, file.getSize(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new IOException("Échec de l'envoi de l'objet vers MinIO (bucket=" + bucket + ") : " + e.getMessage(), e);
        }
        log.info("Objet stocké sur MinIO : {}/{} ({} octets)", bucket, objectKey, file.getSize());

        Image image = new Image();
        image.setName(original);
        image.setType(contentType);
        image.setSize(file.getSize());
        image.setPath(objectKey);
        image.setUrl(publicBaseUrl + "/" + objectKey);
        return image;
    }

    /** Crée le bucket s'il n'existe pas encore (idempotent). */
    private void ensureBucket() throws IOException {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Bucket MinIO créé : {}", bucket);
            }
        } catch (Exception e) {
            throw new IOException("MinIO indisponible ou bucket inaccessible (" + bucket + ") : " + e.getMessage(), e);
        }
    }

    private static String stripTrailingSlash(String value) {
        return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
