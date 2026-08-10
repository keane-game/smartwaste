package sn.smartwaste.collect.tenant.application.service.impl;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.shared.domain.event.UserAccountCreated;
import sn.smartwaste.collect.tenant.application.api.CurrentTenantProvider;
import sn.smartwaste.collect.tenant.domain.model.OrganizationMembership;
import sn.smartwaste.collect.tenant.domain.repository.OrganizationMembershipRepository;

/**
 * Rattache tout nouveau compte à Pikine (ADR-0020 §4, décision du 2026-08-10).
 *
 * <p><b>Le défaut fermé</b> qu'active le filtre {@code organizationFilter} rend invisible toute
 * donnée cloisonnée à un compte sans organisation courante. Sans ce rattachement automatique,
 * chaque habitant nouvellement inscrit (`POST /auth/register`) aurait vu un référentiel vide dès sa
 * première connexion — alors que {@code SecurityConfiguration} garantit explicitement que « la
 * lecture du référentiel reste ouverte : c'est le contenu de l'application mobile ».
 *
 * <p><b>Pourquoi Pikine, sans condition.</b> C'est la seule collectivité active à ce jour ; le jour
 * où une seconde existera, ce choix — quel compte rejoint quelle collectivité à l'inscription —
 * redeviendra une vraie décision produit (invite, code postal, sous-domaine…), pas une extension de
 * ce rattachement automatique.
 *
 * <p>Écouteur <b>synchrone</b> (comme {@code NotificationServiceImpl.sendActivationCode}) : la
 * création du compte et son rattachement réussissent ou échouent ensemble, dans la même transaction
 * que {@code AuthServiceImpl.register}/{@code UserServiceImpl.createUser}.
 */
@Component
public class DefaultOrganizationEnrollmentListener {

    private final OrganizationMembershipRepository membershipRepository;

    public DefaultOrganizationEnrollmentListener(OrganizationMembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    @EventListener
    @Transactional
    public void onUserAccountCreated(UserAccountCreated event) {
        // Idempotent par construction : un compte déjà rattaché (ne devrait jamais arriver pour un
        // compte tout juste créé, mais un événement rejoué ne doit pas produire un doublon en
        // violation de la contrainte unique sur `userid`).
        if (membershipRepository.findByUserId(event.userId()).isPresent()) {
            return;
        }
        var membership = new OrganizationMembership();
        membership.setUserId(event.userId());
        membership.setOrganizationId(CurrentTenantProvider.PIKINE_ORGANIZATION_ID);
        membershipRepository.save(membership);
    }
}
