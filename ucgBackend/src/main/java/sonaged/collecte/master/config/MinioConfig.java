package sonaged.collecte.master.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Client MinIO (stockage objet compatible S3) pour les images/fichiers (P1-3 / ADR-0005).
 *
 * <p>Configuration sous {@code sonaged.storage.minio.*} (voir {@code application.yml}).
 * Le client est inerte tant qu'aucune opération n'est effectuée ; il ne tente pas de
 * contacter le serveur au démarrage.
 */
@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient(
            @Value("${sonaged.storage.minio.endpoint:http://localhost:9000}") String endpoint,
            @Value("${sonaged.storage.minio.access-key:minioadmin}") String accessKey,
            @Value("${sonaged.storage.minio.secret-key:minioadmin}") String secretKey) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
