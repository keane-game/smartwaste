/**
 * Module <b>Administration</b> — <b>non</b> un contexte borné.
 *
 * <p>Regroupe les opérations transverses d'exploitation qui, par nature, traversent tous les
 * domaines : corbeille (consultation, restauration, purge planifiée) et, à terme, l'import des
 * données de référence GeoJSON.
 *
 * <p><b>Pourquoi un module de plus que les 8 contextes de l'ADR-0013.</b> Ces opérations ne sont le
 * métier de personne : la corbeille agit sur <i>toute</i> entité soft-deletable, l'import écrit dans
 * le référentiel territorial <i>et</i> dans le cœur métier déchets. Les ranger dans l'un des
 * contextes lui donnerait autorité sur les autres ; les mettre dans le shared kernel en ferait une
 * dépendance de tout le système, contrôleurs compris. L'ADR-0013 admet déjà deux modules qui ne
 * sont pas des contextes ({@code shared} et {@code config}) : celui-ci est le troisième, dans le
 * même esprit.
 *
 * <p><b>Invariant à préserver : personne ne dépend de ce module.</b> C'est ce qui garantit
 * l'acyclicité malgré sa position transverse — il consomme les autres contextes, jamais l'inverse.
 * {@code SoftDeleteService} est pour cette raison resté dans le shared kernel : les services métier
 * l'utilisent, il ne pouvait donc pas venir ici.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Administration")
package sn.smartwaste.collect.administration;
