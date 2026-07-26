package sonaged.collecte.master;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Chargement du contexte Spring complet.
 *
 * <p>Ce test vivait dans {@code sonaged.collecte.test} : la recherche ascendante de
 * {@code @SpringBootConfiguration} partait de ce package et ne pouvait donc jamais atteindre
 * {@link SonagedApplication} (situé dans le package frère {@code sonaged.collecte.master}) —
 * il échouait quelle que soit l'infrastructure disponible. Il est désormais placé dans le
 * package de la classe d'application, comme le veut la convention Spring Boot.
 *
 * <p><strong>Désactivé</strong> : le chargement du contexte ouvre une vraie connexion
 * PostgreSQL (et Hibernate est en {@code ddl-auto=validate} face aux changelogs Liquibase).
 * Il n'existe ici ni base ni Docker (Testcontainers indisponible). À réactiver — en retirant
 * simplement {@code @Disabled} — sur un poste disposant de la base décrite dans
 * {@code src/main/resources/application.properties}, ou en ajoutant Testcontainers.
 */
@SpringBootTest
@Disabled("Nécessite une base PostgreSQL accessible (aucune BD ni Docker dans cet environnement)")
class SonagedApplicationTests {

	@Test
	void contextLoads() {
	}

}
