package sn.smartwaste.collect.tenant.infrastructure.security;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.tenant.application.api.CurrentTenantProvider;

/**
 * Active le filtre Hibernate {@code organizationFilter} pour la durée de la requête (ADR-0020 §4).
 *
 * <p><b>Défaut fermé.</b> Un compte sans organisation courante ne doit voir aucune ligne cloisonnée,
 * jamais toutes : {@link #NO_ORGANIZATION} est un identifiant qui ne correspond à aucune collectivité
 * réelle, posé explicitement plutôt que de laisser le filtre désactivé (ce qui aurait montré la
 * totalité des données, l'exact inverse du cloisonnement recherché).
 *
 * <p><b>Exception : {@code SUPER_ADMIN} n'est pas filtré</b>, sauf s'il demande explicitement un
 * périmètre via {@value #SCOPE_HEADER}. C'est l'exploitant de la plateforme (SONAGED), pas
 * l'administration d'une seule collectivité — il doit pouvoir superviser l'ensemble sans changer de
 * compte, et se restreindre à une collectivité sans en changer non plus. Cette exception inverse la
 * règle générale de cloisonnement : elle mérite ses tests dédiés.
 *
 * <p><b>Ce filtre a été inopérant jusqu'au 2026-08-11.</b> Mesure : un ADMIN d'une autre
 * collectivité voyait 71 dépotoirs, 52 circuits et 12 communes — exactement les chiffres d'un
 * SUPER_ADMIN. La cause n'était pas ici mais dans l'ordonnancement : {@code open-in-view} était
 * assuré par un <i>intercepteur</i> Spring MVC, appliqué par le {@code DispatcherServlet}, donc
 * après toute la chaîne de filtres. Aucun {@code EntityManager} n'était lié au thread à ce
 * moment-là ({@code emLie=false} à l'instrumentation), si bien que {@code unwrap(Session.class)}
 * ouvrait une session temporaire, y activait le filtre et la jetait. Voir
 * {@code config.PersistenceSessionBindingConfig}, qui lie désormais l'{@code EntityManager} avant
 * ce filtre.
 *
 * <p><b>L'ordre n'est plus implicite.</b> Il valait auparavant {@code LOWEST_PRECEDENCE} par
 * défaut, ce que la javadoc décrivait comme suffisant. Il l'est pour passer après {@code JwtFilter}
 * — dont dépend la résolution de l'utilisateur courant — mais pas pour passer après la liaison de
 * la session. {@code @Order} le rend explicite, et sa relation avec
 * {@link sn.smartwaste.collect.config.PersistenceSessionBindingConfig#OPEN_ENTITY_MANAGER_IN_VIEW_ORDER}
 * devient une contrainte lisible plutôt qu'une coïncidence.
 *
 * <p><b>Pourquoi ce n'est PAS câblé dans {@code SecurityConfiguration} (module {@code identity}).</b>
 * Le contexte {@code tenant} dépend déjà de {@code identity} pour résoudre l'utilisateur courant
 * ({@code CurrentTenantProviderImpl} → {@code CurrentUserProvider}) — un appel en sens inverse
 * depuis {@code SecurityConfiguration} vers ce filtre formerait une dépendance circulaire entre
 * modules, que Spring Modulith refuse. Enregistré comme filtre servlet ordinaire, il n'introduit
 * aucune dépendance nouvelle dans un sens comme dans l'autre.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class TenantFilterActivationFilter extends OncePerRequestFilter {

    /** N'existe dans aucun changelog : ferme la vue par défaut plutôt que de l'ouvrir. */
    private static final UUID NO_ORGANIZATION = new UUID(0L, 0L);

    /**
     * En-tête par lequel un SUPER_ADMIN choisit la collectivité qu'il observe.
     *
     * <p>Un en-tête plutôt qu'un paramètre de requête : le périmètre est transverse à tous les
     * endpoints, et le poser en paramètre obligerait chaque contrôleur à l'accepter et à le
     * transmettre. Il n'a d'effet que pour un SUPER_ADMIN (voir {@link #resolveScope}).
     */
    public static final String SCOPE_HEADER = "X-Organization-Id";

    @PersistenceContext
    private EntityManager entityManager;

    private final CurrentUserProvider currentUserProvider;
    private final CurrentTenantProvider currentTenantProvider;

    public TenantFilterActivationFilter(CurrentUserProvider currentUserProvider,
                                        CurrentTenantProvider currentTenantProvider) {
        this.currentUserProvider = currentUserProvider;
        this.currentTenantProvider = currentTenantProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        resolveScope(request).ifPresent(organizationId ->
                entityManager.unwrap(Session.class)
                        .enableFilter("organizationFilter")
                        .setParameter("organizationId", organizationId));
        filterChain.doFilter(request, response);
    }

    /**
     * Collectivité sur laquelle cloisonner cette requête, ou {@link Optional#empty()} pour ne pas
     * filtrer du tout.
     *
     * <p>Trois cas, et un seul rend {@code empty} :
     * <ol>
     *   <li><b>SUPER_ADMIN sans en-tête de périmètre</b> — vue plateforme complète, aucun filtre.
     *       C'est l'exception qui inverse la règle générale ; elle est délibérée.</li>
     *   <li><b>SUPER_ADMIN avec {@value #SCOPE_HEADER}</b> — il observe une collectivité en
     *       particulier. Le filtre s'applique à la valeur demandée, sans qu'il change de compte.
     *       Un en-tête illisible est traité comme {@link #NO_ORGANIZATION} : un périmètre demandé
     *       mais incompréhensible ne doit pas ouvrir la vue en grand.</li>
     *   <li><b>Tout autre compte</b> — cloisonné sur sa propre collectivité, et sur
     *       {@link #NO_ORGANIZATION} s'il n'en a aucune. L'en-tête est <b>ignoré</b> : le laisser
     *       agir donnerait à n'importe quel ADMIN le moyen de lire une autre collectivité en
     *       ajoutant une ligne à sa requête.</li>
     * </ol>
     */
    private Optional<UUID> resolveScope(HttpServletRequest request) {
        if (!currentUserProvider.currentUserHasAnyRole("SUPER_ADMIN")) {
            return Optional.of(currentTenantProvider.currentOrganizationId().orElse(NO_ORGANIZATION));
        }
        String requested = request.getHeader(SCOPE_HEADER);
        if (requested == null || requested.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(requested.trim()));
        } catch (IllegalArgumentException malformed) {
            return Optional.of(NO_ORGANIZATION);
        }
    }
}
