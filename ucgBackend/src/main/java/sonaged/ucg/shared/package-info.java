/**
 * Shared kernel — module <b>ouvert</b> (accessible par tous les autres modules).
 *
 * <p>Contient uniquement des éléments réellement transverses : base d'audit
 * ({@code AbstractAuditingEntity}), value objects géo ({@code Geometry}, {@code Coordinate}),
 * <b>contrats d'événements de domaine</b> échangés entre modules, et enums transverses.
 * Volontairement minimal pour éviter tout couplage caché (ADR-0012).
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Shared Kernel",
        type = org.springframework.modulith.ApplicationModule.Type.OPEN
)
package sonaged.ucg.shared;
