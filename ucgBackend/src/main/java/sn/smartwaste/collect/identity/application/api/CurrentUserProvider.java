package sn.smartwaste.collect.identity.application.api;

import java.util.UUID;

/**
 * Identifiant de l'utilisateur authentifié sur la requête en cours.
 *
 * <p>Permet aux autres contextes d'attribuer une donnée à son auteur sans connaître le modèle
 * d'identité : ils ne manipulent qu'un {@link UUID} (ADR-0012 — référence par identifiant).
 */
public interface CurrentUserProvider {

    /**
     * @return l'identifiant de l'utilisateur authentifié
     * @throws IllegalStateException si la requête n'est pas authentifiée — c'est un défaut de
     *         configuration de la chaîne de sécurité, pas une erreur métier de l'appelant.
     */
    UUID requireCurrentUserId();

    /**
     * Vrai si l'utilisateur courant porte l'un de ces rôles (sans préfixe {@code ROLE_}).
     *
     * <p>Publié parce que d'autres contextes doivent distinguer un agent, borné à son territoire,
     * d'un administrateur qui supervise les 12 communes. Le contrat ne rend qu'un booléen : aucun
     * appelant ne voit le compte ni ses habilitations.
     */
    boolean currentUserHasAnyRole(String... roles);
}
