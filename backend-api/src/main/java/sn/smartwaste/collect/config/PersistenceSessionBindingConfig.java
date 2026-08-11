package sn.smartwaste.collect.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;

/**
 * Lie l'{@code EntityManager} au thread <b>avant</b> les filtres applicatifs.
 *
 * <p><b>Pourquoi ce fichier existe.</b> Le cloisonnement multi-tenant (ADR-0020) repose sur un
 * filtre Hibernate activé par requête dans {@code TenantFilterActivationFilter}. Ce filtre était
 * <b>entièrement inopérant</b> : mesuré le 2026-08-11, un ADMIN d'une autre collectivité voyait
 * 100 % des données de Pikine (71 dépotoirs, 52 circuits, 12 communes — les mêmes chiffres qu'un
 * SUPER_ADMIN).
 *
 * <p><b>La cause.</b> Spring Boot n'active pas {@code open-in-view} par un filtre servlet mais par
 * un {@code OpenEntityManagerInViewInterceptor}, appliqué par le {@code DispatcherServlet} —
 * c'est-à-dire <b>après</b> toute la chaîne de filtres. Quand le filtre tenant s'exécutait, aucun
 * {@code EntityManager} n'était donc lié au thread : {@code entityManager.unwrap(Session.class)}
 * ouvrait une session temporaire, y activait le filtre, et la jetait. Les requêtes de la requête
 * HTTP s'exécutaient ensuite sur une <i>autre</i> session, sans filtre. Instrumentation à l'appui :
 * {@code emLie=false} sur chaque appel, et un hash de session identique sur tous les threads.
 *
 * <p><b>Le correctif.</b> Enregistrer explicitement {@link OpenEntityManagerInViewFilter} — la
 * variante <i>filtre</i> du même mécanisme — à une précédence supérieure à celle du filtre tenant.
 * L'{@code EntityManager} est alors lié au thread avant lui, {@code unwrap} rend la session
 * réellement utilisée par la requête, et {@code enableFilter} porte enfin sur les bonnes requêtes.
 * Le {@code JpaTransactionManager} rejoint la session déjà liée : les méthodes
 * {@code @Transactional} en aval ne s'en ouvrent pas une nouvelle.
 *
 * <p>{@code spring.jpa.open-in-view} est mis à {@code false} dans {@code application.yml} pour que
 * l'intercepteur de Spring Boot ne fasse pas double emploi avec ce filtre. Le comportement
 * fonctionnel est identique — c'est le même mécanisme, à un autre point de la chaîne — et
 * l'avertissement au démarrage disparaît au passage.
 *
 * <p>Ce fichier vit dans le non-contexte {@code config} et n'importe aucun contexte métier : il ne
 * crée donc aucune arête nouvelle dans le graphe de modules (ADR-0024).
 */
@Configuration
public class PersistenceSessionBindingConfig {

    /**
     * Précédence du filtre {@code open-in-view}. Doit rester <b>strictement inférieure</b> à celle
     * de {@code TenantFilterActivationFilter} : un filtre de précédence plus faible s'exécute plus
     * tôt, donc plus à l'extérieur de la chaîne. Inverser les deux remettrait exactement le défaut
     * que cette classe corrige.
     */
    public static final int OPEN_ENTITY_MANAGER_IN_VIEW_ORDER = Ordered.LOWEST_PRECEDENCE - 100;

    @Bean
    public FilterRegistrationBean<OpenEntityManagerInViewFilter> openEntityManagerInViewFilter() {
        var registration = new FilterRegistrationBean<>(new OpenEntityManagerInViewFilter());
        registration.setOrder(OPEN_ENTITY_MANAGER_IN_VIEW_ORDER);
        return registration;
    }
}
