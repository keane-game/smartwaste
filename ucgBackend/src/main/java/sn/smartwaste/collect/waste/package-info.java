/**
 * Module <b>Gestion des Déchets</b> (cœur opérationnel).
 *
 * <p>Points de collecte (dépotoirs, bacs, mobilier urbain), alertes de remplissage et circuits de
 * collecte / balayage. C'est le contexte qui porte la valeur métier du produit ; les autres
 * existent pour l'alimenter ou l'observer.
 *
 * <p>L'ingestion des mesures capteurs reste un <b>module séparé</b> ({@code iot}) et non un
 * sous-domaine d'ici, pour pouvoir la faire évoluer et l'extraire indépendamment de sa forte
 * volumétrie.
 *
 * <p>Découplage (ADR-0012) : référence {@code communeId}, {@code quartierId}, {@code userId} par
 * identifiant.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Gestion des Déchets")
package sn.smartwaste.collect.waste;
