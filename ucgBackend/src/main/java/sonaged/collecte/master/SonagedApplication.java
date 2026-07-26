package sonaged.collecte.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Point d'entrée de l'application.
 *
 * <p><b>Périmètre de scan élargi à {@code sonaged} (P1-7b).</b> Par défaut, Spring Boot ne
 * scanne que le package de cette classe et ses sous-packages, soit {@code sonaged.collecte.master}.
 * Or la migration vers le monolithe modulaire déplace le code, module par module, vers
 * {@code sonaged.ucg.*} : sans cet élargissement, chaque classe déplacée cesserait
 * silencieusement d'être un bean (ou une entité, ou un repository) — l'application démarrerait
 * puis échouerait à l'exécution, sans erreur de compilation pour le signaler.
 *
 * <p>{@code sonaged} est l'ancêtre commun des deux arborescences : il couvre donc à la fois le
 * code hérité et le code déjà migré, pendant toute la durée de la transition.
 *
 * <p>{@code @EntityScan} et {@code @EnableJpaRepositories} sont déclarés explicitement pour la
 * même raison : ils remplacent la détection automatique, qui reste sinon calée sur le package
 * de cette classe.
 */
@SpringBootApplication(scanBasePackages = "sonaged")
@EntityScan(basePackages = "sonaged")
@EnableJpaRepositories(basePackages = "sonaged")
@EnableScheduling // purge planifiée du soft-delete (DeletionPurgeScheduler)
public class SonagedApplication {

	public static void main(String[] args) {
		SpringApplication.run(SonagedApplication.class, args);
	}


}
