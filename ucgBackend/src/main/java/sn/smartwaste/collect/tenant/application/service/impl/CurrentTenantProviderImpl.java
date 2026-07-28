package sn.smartwaste.collect.tenant.application.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.tenant.application.api.CurrentTenantProvider;
import sn.smartwaste.collect.tenant.domain.model.OrganizationMembership;
import sn.smartwaste.collect.tenant.domain.repository.OrganizationMembershipRepository;

/**
 * Résout la collectivité courante à partir de l'utilisateur authentifié.
 *
 * <p><b>Pourquoi pas un filtre + ThreadLocal</b>, qui serait la solution réflexe. D'abord parce que
 * le filtre devrait être inséré dans la chaîne Spring Security, déclarée par
 * {@code SecurityConfiguration} — qui appartient au contexte « Identité &amp; Accès ». Ce module
 * dépendrait donc du tenant, lequel dépend déjà de lui pour connaître l'utilisateur :
 * <b>un cycle</b>, que {@code modules.verify()} refuserait à juste titre. Ensuite parce qu'un
 * {@code ThreadLocal} mal nettoyé fuit d'une requête à l'autre sur un pool de threads — et fuiter
 * un identifiant de tenant, c'est servir les données d'une collectivité à une autre.
 *
 * <p>La résolution est donc <b>paresseuse</b> : une lecture indexée sur {@code userId}, faite au
 * moment où la question est posée. Si le volume d'appels le justifiait, la parade serait un cache
 * de portée requête — pas un {@code ThreadLocal} manuel.
 */
@Service
@Transactional(readOnly = true)
public class CurrentTenantProviderImpl implements CurrentTenantProvider {

    private final CurrentUserProvider currentUserProvider;
    private final OrganizationMembershipRepository membershipRepository;

    public CurrentTenantProviderImpl(CurrentUserProvider currentUserProvider,
                                     OrganizationMembershipRepository membershipRepository) {
        this.currentUserProvider = currentUserProvider;
        this.membershipRepository = membershipRepository;
    }

    @Override
    public Optional<UUID> currentOrganizationId() {
        UUID userId;
        try {
            userId = currentUserProvider.requireCurrentUserId();
        } catch (IllegalStateException notAuthenticated) {
            // Requête non authentifiée (endpoint public) : pas de collectivité, et ce n'est pas
            // une anomalie. On rend `empty` plutôt que de propager — l'appelant décidera.
            return Optional.empty();
        }
        return membershipRepository.findByUserId(userId)
                .map(OrganizationMembership::getOrganizationId);
    }
}
