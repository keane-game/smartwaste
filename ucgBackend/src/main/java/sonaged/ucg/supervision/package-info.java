/**
 * Module <b>Supervision</b> (read-side / BFF, en aval).
 *
 * <p>Tableaux de bord, statistiques, historique/audit, rapports. Construit des <b>modèles de lecture</b>
 * par abonnement aux événements de domaine — n'écrit dans aucun domaine. Entité cible : {@code HistoryEntity}
 * + tables de projection.
 *
 * <p>Découplage (ADR-0012) : n'accède qu'aux événements/API publiques, pas aux entités des autres modules.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Supervision")
package sonaged.ucg.supervision;
