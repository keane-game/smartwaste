package sn.smartwaste.collect.platform.application.service;

import java.util.List;
import java.util.UUID;

/** Abonnement des habitants aux passages de collecte de leur quartier. */
public interface CollectionSubscriptionService {

    /**
     * Abonne l'utilisateur authentifié au quartier donné. Idempotent : réabonner réactive.
     *
     * <p>Aucune adresse en paramètre : le destinataire est l'utilisateur du jeton, et son adresse
     * est résolue à l'envoi. Accepter une adresse du client permettait d'abonner un tiers.
     */
    void subscribe(UUID quartierId);

    /** Désabonne l'utilisateur authentifié. Idempotent. */
    void unsubscribe(UUID quartierId);

    /** Quartiers auxquels l'utilisateur authentifié est abonné. */
    List<UUID> mySubscriptions();
}
