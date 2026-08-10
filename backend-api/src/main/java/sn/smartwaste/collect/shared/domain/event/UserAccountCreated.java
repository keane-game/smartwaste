package sn.smartwaste.collect.shared.domain.event;

import java.util.UUID;

/**
 * Événement de domaine : un compte vient d'être créé — auto-inscription (`/auth/register`) ou
 * création par un administrateur (`POST /v1/users`), les deux seuls chemins de création de compte
 * (ADR-0020).
 *
 * <p>Rompt le cycle que créerait un appel direct <b>Identité &amp; Accès → Multi-tenant</b> : le
 * contexte {@code tenant} dépend déjà de {@code identity} pour résoudre l'utilisateur courant
 * ({@code CurrentTenantProviderImpl}), donc l'inverse formerait une dépendance circulaire que Spring
 * Modulith refuse. Même raisonnement, même remède que pour {@link ActivationCodeIssued} entre
 * Identité et Communication.
 *
 * @param userId identifiant du compte créé
 */
public record UserAccountCreated(UUID userId) { }
