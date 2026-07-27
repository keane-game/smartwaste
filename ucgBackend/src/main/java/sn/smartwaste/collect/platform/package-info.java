/**
 * Module <b>Plateforme</b> (support, en aval).
 *
 * <p>Services transverses au produit : signalements citoyens ({@code Avis}) et notifications
 * sortantes (e-mail, SSE temps réel, push). Regroupés ici parce qu'ils ne portent aucune règle
 * métier déchet — ce sont des canaux de sortie, pilotés par les événements des autres contextes.
 *
 * <p>Découplage (ADR-0012) : référence {@code userId} / {@code depotoirId} par identifiant et
 * réagit aux événements de domaine plutôt que d'être appelé directement par leurs émetteurs.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Plateforme")
package sn.smartwaste.collect.platform;
