package sonaged.collecte.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Point d'entrée de l'application.
 *
 * <p><b>Périmètre de scan élargi à {@code sonaged} et {@code sn.smartwaste.collect}.</b> Par défaut,
 * Spring Boot ne scanne que le package de cette classe et ses sous-packages, soit
 * {@code sonaged.collecte.master}. Or la migration vers le monolithe modulaire déplace le code,
 * module par module, vers {@code sn.smartwaste.collect.*} : sans cet élargissement, chaque classe
 * déplacée cesserait silencieusement d'être un bean (ou une entité, ou un repository) —
 * l'application démarrerait puis échouerait à l'exécution, sans erreur de compilation pour le
 * signaler.
 *
 * <p>Les deux racines sont listées car elles n'ont <b>pas d'ancêtre commun</b> : {@code sonaged}
 * couvre le code hérité et le squelette intermédiaire {@code sonaged.ucg},
 * {@code sn.smartwaste.collect} la structure DDD cible. Les deux doivent être scannées pendant
 * toute la durée de la transition.
 *
 * <p>{@code @EntityScan} et {@code @EnableJpaRepositories} sont déclarés explicitement pour la
 * même raison : ils remplacent la détection automatique, qui reste sinon calée sur le package
 * de cette classe.
 */
@SpringBootApplication(scanBasePackages = {"sonaged", "sn.smartwaste.collect"})
@EntityScan(basePackages = {"sonaged", "sn.smartwaste.collect"})
@EnableJpaRepositories(basePackages = {"sonaged", "sn.smartwaste.collect"})
@EnableScheduling // purge planifiée du soft-delete (DeletionPurgeScheduler)
public class SonagedApplication {

	public static void main(String[] args) {
		SpringApplication.run(SonagedApplication.class, args);
	}


}
