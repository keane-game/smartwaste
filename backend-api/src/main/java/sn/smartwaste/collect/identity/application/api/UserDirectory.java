package sn.smartwaste.collect.identity.application.api;

import java.util.Optional;
import java.util.UUID;

/**
 * Consultation d'un compte utilisateur par les autres contextes.
 *
 * <p><b>Pourquoi ce port existe.</b> L'adresse e-mail d'un utilisateur appartient au contexte
 * « Identité &amp; Accès », et c'est le seul endroit où elle est <b>prouvée</b> : l'inscription
 * envoie un code d'activation à cette adresse, et le compte ne s'ouvre qu'une fois le code saisi.
 * Tout autre contexte qui aurait sa propre copie — ou pire, qui l'accepterait du client —
 * contournerait cette preuve.
 */
public interface UserDirectory {

    /**
     * Adresse e-mail vérifiée d'un utilisateur.
     *
     * @return {@link Optional#empty()} si le compte n'existe plus — un abonnement peut survivre à
     *         la suppression de son titulaire, et l'appelant doit alors simplement s'abstenir.
     */
    Optional<String> emailOf(UUID userId);
}
