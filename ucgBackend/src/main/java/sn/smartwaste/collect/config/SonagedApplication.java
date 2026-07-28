package sn.smartwaste.collect.config;

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
 * <p>Les deux racines sont listées car elles n'ont <b>pas d'ancêtre commun</b>. La migration
 * ADR-0013 est désormais <b>terminée à une exception près</b> : il ne reste dans
 * {@code sonaged.collecte.master} que l'<b>import GeoJSON</b> (7 fichiers). Il y est maintenu
 * volontairement — le déplacer dans {@code administration} imposerait d'exposer les entités
 * <i>et</i> les repositories de {@code territory} et de {@code waste}, dans lesquels il écrit
 * directement, c'est-à-dire exactement l'anti-pattern écarté partout ailleurs. Sa migration
 * suppose de le recâbler sur les services applicatifs de chaque contexte : un refactoring, pas un
 * déplacement de packages.
 *
 * <p>{@code sonaged.ucg} ne contient plus que l'échafaudage mort de l'ADR-0010 (aucun bean).
 * Le jour où l'import est recâblé, ce scan peut se réduire à {@code sn.smartwaste.collect}.
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
