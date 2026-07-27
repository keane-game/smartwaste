/**
 * Module <b>Supervision &amp; Analytique</b> (read-side / BFF, en aval).
 *
 * <p>Tableaux de bord, indicateurs, historique et rapports. Contexte strictement <b>en lecture</b> :
 * il n'écrit dans aucun domaine, ce qui garantit qu'un besoin d'affichage ne vienne jamais
 * déformer le modèle métier amont.
 *
 * <p>Découplage (ADR-0012) : à terme il se construira par abonnement aux événements de domaine et
 * n'accédera plus qu'aux API publiques des autres modules.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Supervision & Analytique")
package sn.smartwaste.collect.analytics;
