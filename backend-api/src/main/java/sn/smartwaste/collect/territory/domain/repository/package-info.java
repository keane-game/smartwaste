/**
 * Interface nommée <b>{@code repositories}</b> du module « Référentiel Territorial ».
 *
 * <p>Par défaut, Spring Modulith considère tout sous-package d'un module comme <i>interne</i> :
 * seul le package racine du module est exposé. Sans cette déclaration, la migration du référentiel
 * territorial rendrait invisibles des types dont un autre module dépend déjà — le module
 * « Supervision &amp; Analytique » compte les communes, départements, régions et quartiers via ces
 * repositories — et {@code modules.verify()} échouerait sur des accès aux internals.
 *
 * <p><b>Concession transitoire, volontairement nommée.</b> Publier des repositories n'est pas la
 * cible : l'ADR-0012 prévoit que la supervision se construise par abonnement aux événements de
 * domaine, ou à défaut via une API applicative en lecture. La déclarer explicitement ici plutôt
 * que de relâcher la vérification globale garde la dette visible et localisée : le jour où
 * l'analytique passe par les événements, ce fichier disparaît et la frontière se referme d'elle-même.
 */
@org.springframework.modulith.NamedInterface("repositories")
package sn.smartwaste.collect.territory.domain.repository;
