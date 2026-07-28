package sn.smartwaste.collect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Point d'entrée de l'application.
 *
 * <p><b>Périmètre de scan.</b> La migration ADR-0013 est <b>terminée</b> : tout le code applicatif
 * vit sous {@code sn.smartwaste.collect}. Cette classe est placée à la <b>racine</b> de ce package,
 * comme le veut la convention Spring Boot — c'est ce qui rend le scan par
 * défaut de Spring Boot suffit donc — plus besoin de {@code scanBasePackages}, {@code @EntityScan}
 * ni {@code @EnableJpaRepositories} explicites, qui ne servaient qu'à couvrir les deux racines
 * pendant la transition.
 *
 * <p>{@code sonaged.ucg} subsiste, mais ne contient que l'échafaudage mort de l'ADR-0010
 * (classes marqueurs et {@code package-info}, aucun bean) : ne plus le scanner est sans effet.
 */
@SpringBootApplication
@EnableScheduling // purge planifiée du soft-delete (DeletionPurgeScheduler)
public class SonagedApplication {

	public static void main(String[] args) {
		SpringApplication.run(SonagedApplication.class, args);
	}


}
