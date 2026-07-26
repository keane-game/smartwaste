package sonaged.collecte.master.enums;

/**
 * Statut de suppression logique (soft-delete) d'une ressource.
 *
 * <ul>
 *   <li>{@link #ACTIVE} — ressource normale, visible.</li>
 *   <li>{@link #PENDING_DELETION} — supprimée logiquement ; conservée pendant une période de
 *       rétention (30 j par défaut) avant purge définitive ; restaurable tant que le délai court.</li>
 * </ul>
 */
public enum DeletionStatus {
    ACTIVE,
    PENDING_DELETION
}
