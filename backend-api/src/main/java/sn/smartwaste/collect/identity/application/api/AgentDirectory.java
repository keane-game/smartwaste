package sn.smartwaste.collect.identity.application.api;

import java.util.List;
import java.util.UUID;

/**
 * Territoires couverts par un agent de collecte (G1 du backlog).
 *
 * <p>Publié parce que le contexte « Déchets » doit savoir, au moment où un agent déclare un
 * passage, s'il est bien habilité sur la commune du point. Le contrat n'expose que des
 * identifiants : aucun autre module ne voit l'affectation ni le compte.
 */
public interface AgentDirectory {

    /** Communes couvertes par cet agent, vide s'il n'est affecté nulle part. */
    List<UUID> communesOf(UUID agentId);

    /** Vrai si l'agent est habilité sur cette commune. */
    boolean covers(UUID agentId, UUID communeId);
}
