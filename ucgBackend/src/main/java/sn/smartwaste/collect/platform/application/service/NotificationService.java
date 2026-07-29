package sn.smartwaste.collect.platform.application.service;

import sn.smartwaste.collect.shared.domain.event.ActivationCodeIssued;

/**
 * Envoi des notifications sortantes (contexte <b>Communication</b>).
 *
 * <p>Le contrat porte désormais un événement autoporteur, et non l'entité {@code Validation} :
 * la communication n'a plus à connaître le modèle du contexte Identité &amp; Accès (P1-7b).
 */
public interface NotificationService {

    /** Envoie le code d'activation au destinataire décrit par l'événement. */
    void sendActivationCode(ActivationCodeIssued event);

    /**
     * Previent un habitant du passage imminent du camion dans son quartier.
     *
     * <p>Reponse directe a la demande n°1 de l'enquete citoyenne — et remplacement du klaxon,
     * que la majorite des repondants declare genant.
     */
    void sendCollectionReminder(String recipientEmail, java.time.LocalTime passageTime);
}
