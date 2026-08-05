/**
 * Module <b>Ingestion IoT</b> (ADR-0004).
 *
 * <p>Registre des capteurs et reception des mesures transmises depuis le terrain. C'est le point
 * d'entree du coeur metier : sans lui, « detecter le niveau de remplissage » n'existait pas et
 * {@code Alert} n'etait qu'une ressource saisie a la main.
 *
 * <p><b>Perimetre volontairement etroit.</b> Ce module <i>recoit et conserve</i> ; il n'interprete
 * rien. Il ne connait aucun seuil, ne cree aucune alerte, ne sait pas ce qu'est un circuit. Il
 * publie {@link sn.smartwaste.collect.shared.domain.event.MeasurementRecorded} et s'arrete la.
 * La regle « a partir de quand faut-il alerter » appartient au contexte « Dechets ».
 *
 * <p><b>Pourquoi cette frontiere est la plus stricte du systeme.</b> C'est le module dont le volume
 * croitra le plus vite — une mesure par capteur et par intervalle, sur des milliers de points — et
 * l'ADR-0013 en fait le <b>premier candidat a l'extraction</b> en microservice. Tout ce qu'on y
 * laisserait deborder devrait etre demele le jour venu.
 *
 * <p><b>Authentification par cle de device</b>, distincte du JWT utilisateur : un capteur n'a pas de
 * session. La cle est stockee hachee (SHA-256), jamais en clair.
 *
 * <p>Decouplage (ADR-0012) : {@code depotoirId} est une reference par identifiant ; aucune FK ne
 * traverse la frontiere vers le contexte « Dechets ».
 */
@org.springframework.modulith.ApplicationModule(displayName = "Ingestion IoT")
package sn.smartwaste.collect.iot;
