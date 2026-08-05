package sn.smartwaste.collect.waste.application.service.impl;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import sn.smartwaste.collect.identity.application.api.AgentDirectory;
import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;

/**
 * Borne un agent de collecte à son territoire (G1 du backlog).
 *
 * <p><b>Pourquoi ce composant est unique.</b> La même règle vaut pour lire une tournée et pour
 * déclarer un passage. L'écrire deux fois garantirait qu'un jour l'une des deux copies évolue sans
 * l'autre — et l'écart s'ouvrirait du côté qu'on ne regarde pas.
 *
 * <p><b>Ce que cela ferme.</b> L'affectation territoriale ({@code AgentAssignment},
 * {@code AgentDirectory}) avait été créée puis laissée <b>inerte</b> : l'autorisation s'arrêtait au
 * rôle, si bien que n'importe quel agent pouvait déclarer collecté n'importe lequel des 71 points,
 * dans n'importe quelle commune. Or une remise à zéro du niveau referme l'alerte et sort le point
 * de la tournée : de quoi faire disparaître un débordement réel depuis un compte étranger au
 * terrain concerné. C'est précisément le travers que ce dépôt connaît déjà — treize règles
 * d'autorisation déclarées que rien n'applique.
 *
 * <p>L'administration n'est pas bornée : elle supervise les 12 communes, et lui imposer une
 * affectation la bloquerait sur son propre outil.
 */
@Component
public class TerritorialAccessGuard {

    private final CurrentUserProvider currentUserProvider;
    private final AgentDirectory agentDirectory;

    public TerritorialAccessGuard(CurrentUserProvider currentUserProvider,
                                  AgentDirectory agentDirectory) {
        this.currentUserProvider = currentUserProvider;
        this.agentDirectory = agentDirectory;
    }

    /** Vrai si l'appelant supervise l'ensemble du territoire. */
    public boolean isAdministration() {
        return currentUserProvider.currentUserHasAnyRole("ADMIN", "SUPER_ADMIN");
    }

    /**
     * @param communeId commune visée, {@code null} pour un point qui n'en a pas
     * @throws AccessDeniedException si l'appelant n'est pas habilité sur cette commune
     */
    public void requireAccessTo(UUID communeId) {
        if (isAdministration()) {
            return;
        }
        // Un territoire inexistant n'est couvert par personne : 15 des 71 points importés n'ont pas
        // de commune, faute de libellés concordants entre les fichiers source et le référentiel.
        // Les ouvrir à tous rouvrirait le trou par la porte de derrière.
        if (communeId == null || !agentDirectory.covers(currentUserProvider.requireCurrentUserId(),
                                                        communeId)) {
            throw new AccessDeniedException("Territoire hors des communes affectees");
        }
    }
}
