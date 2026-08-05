/**
 * Shared kernel — module <b>ouvert</b> (accessible par tous les autres modules).
 *
 * <p>Contient uniquement des éléments réellement transverses : base d'audit, value objects
 * géographiques, <b>contrats d'événements de domaine</b> échangés entre modules et enums
 * transverses. Volontairement minimal : tout ce qu'on y place devient une dépendance de tout le
 * système et donc un couplage qu'on ne peut plus voir (ADR-0012).
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Shared Kernel",
        type = org.springframework.modulith.ApplicationModule.Type.OPEN
)
package sn.smartwaste.collect.shared;
