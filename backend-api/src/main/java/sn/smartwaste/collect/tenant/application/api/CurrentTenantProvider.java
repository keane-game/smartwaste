package sn.smartwaste.collect.tenant.application.api;

import java.util.Optional;
import java.util.UUID;

/**
 * Collectivité pour le compte de laquelle la requête en cours est traitée.
 *
 * <p>C'est la brique que l'ADR-0013 §4 demande de poser <i>avant</i> d'être exploitée : les
 * agrégats métier ne portent pas encore de {@code tenantId} (P2-3, XL), mais le contexte est
 * disponible et testé, de sorte que le premier agrégat à l'adopter n'aura pas à l'inventer.
 */
public interface CurrentTenantProvider {

    /**
     * @return la collectivité de l'utilisateur authentifié, ou {@link Optional#empty()} si la
     *         requête n'est pas authentifiée ou si l'utilisateur n'est rattaché à aucune
     *         collectivité. <b>Un {@code Optional} vide n'autorise rien</b> : l'appelant qui
     *         cloisonne doit refuser, jamais élargir au global.
     */
    Optional<UUID> currentOrganizationId();
}
