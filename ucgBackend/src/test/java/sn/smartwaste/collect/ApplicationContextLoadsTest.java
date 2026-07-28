package sn.smartwaste.collect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.platform.application.api.AlertStreamMetrics;
import sn.smartwaste.collect.territory.application.api.TerritoryReadModel;
import sn.smartwaste.collect.waste.application.api.WasteReadModel;

import sonaged.collecte.master.SonagedApplication;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Démarre le <b>contexte Spring complet</b> — le test qui manquait.
 *
 * <p><b>Ce qu'il ferme comme trou.</b> Jusqu'ici, aucun test n'instanciait le contexte : le seul qui
 * l'aurait fait ({@code SonagedApplicationTests}) était {@code @Disabled}, faute de PostgreSQL. Toute
 * la classe d'erreurs « ça compile mais ça ne démarre pas » — bean manquant, injection ambiguë,
 * référence circulaire, classe déplacée hors du périmètre de {@code scanBasePackages} — n'était donc
 * détectable qu'au premier lancement réel. C'est nommément le risque que l'ADR-0013 s'attribue :
 * « une régression de câblage Spring ne serait visible qu'au démarrage ». Il est d'autant plus vif
 * que la migration déplace des centaines de classes entre deux racines de packages.
 *
 * <p><b>Pourquoi H2 et pas Testcontainers.</b> Il n'y a ni base ni Docker dans cet environnement.
 * H2 en mode compatibilité PostgreSQL suffit à l'objectif : on veut savoir si le contexte se câble,
 * pas si le SQL est identique. Liquibase est désactivé et le schéma est dérivé des entités
 * ({@code create-drop}) — sans quoi les changelogs, qui utilisent {@code uuid} natif et
 * {@code dropPrimaryKey}, échoueraient sur H2 pour de mauvaises raisons.
 *
 * <p><b>Ce que ce test ne couvre PAS, et qu'il ne faut pas lui prêter</b> : la correspondance entre
 * les entités et le schéma Liquibase. Elle reste vérifiée par {@code ddl-auto: validate} au
 * démarrage réel contre PostgreSQL, qui n'a toujours jamais été exercé. Un schéma dérivé des
 * entités est cohérent avec elles <i>par construction</i> — c'est justement ce que
 * {@code validate} sert à contredire.
 */
// `classes = ...` est indispensable : la recherche ascendante de @SpringBootConfiguration part de
// `sn.smartwaste.collect` et ne peut pas atteindre `SonagedApplication`, resté dans la racine
// héritée `sonaged.collecte.master`. C'est exactement le piège qui rendait l'ancien test
// inopérant — une raison de plus pour que la classe d'application rejoigne la racine cible.
@SpringBootTest(classes = SonagedApplication.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:smartwaste;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        // La purge planifiée et le heartbeat SSE n'ont rien à faire dans un test de câblage.
        "sonaged.deletion.purge-cron=0 0 5 31 2 ?",
        "sonaged.alerts.stream.heartbeat-ms=3600000"
})
class ApplicationContextLoadsTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("le contexte Spring démarre avec les deux racines de packages")
    void contextLoads() {
        assertThat(context).isNotNull();
        // Garde-fou contre un contexte qui démarrerait « à vide » si `scanBasePackages` cessait de
        // couvrir une racine : c'est précisément le mode de panne silencieux redouté.
        assertThat(context.getBeanDefinitionCount()).isGreaterThan(100);
    }

    @Test
    @DisplayName("les deux racines de packages sont bien scannées")
    void bothPackageRootsAreScanned() {
        // Racine cible.
        assertThat(context.getBeansOfType(WasteReadModel.class)).isNotEmpty();
        // Racine héritée : l'import GeoJSON y vit encore. S'il cessait d'être un bean, rien ne le
        // signalerait à la compilation.
        assertThat(context.containsBean("uploadFileServiceImpl"))
                .as("le code hérité doit rester scanné pendant la transition")
                .isTrue();
    }

    @Test
    @DisplayName("chaque interface publiée par un contexte a exactement une implémentation")
    void publishedPortsAreResolvable() {
        // Ces ports sont le contrat entre modules : une implémentation manquante ou dupliquée
        // ferait échouer l'injection au démarrage, pas à la compilation.
        assertThat(context.getBeansOfType(WasteReadModel.class)).hasSize(1);
        assertThat(context.getBeansOfType(TerritoryReadModel.class)).hasSize(1);
        assertThat(context.getBeansOfType(CurrentUserProvider.class)).hasSize(1);
        assertThat(context.getBeansOfType(AlertStreamMetrics.class)).hasSize(1);
    }
}
