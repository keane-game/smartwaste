/**
 * Module <b>Ingestion IoT</b> — ⚠️ <b>RÉSERVÉ / NON IMPLÉMENTÉ</b>.
 *
 * <p>Capteurs de niveau de remplissage, ingestion des mesures et moteur de seuils. La brique
 * <b>n'existe pas encore</b> dans le produit : ce module est déclaré uniquement pour réserver la
 * frontière et la couture d'extraction (fort volume → premier microservice à extraire, ADR-0010).
 * Le déclarer maintenant évite qu'à l'arrivée de l'IoT le code ne se greffe par facilité dans
 * {@code waste}, d'où il ne ressortirait plus.
 *
 * <p>Il ne contient <b>aucun composant Spring, aucun endpoint, aucune entité</b>. À ne peupler que
 * lorsque la fonctionnalité sera décidée (ROADMAP P0-5/P0-6).
 *
 * <p>Découplage (ADR-0012) : référencera {@code depotoirId} / {@code capteurId} par identifiant.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Ingestion IoT (réservé)")
package sn.smartwaste.collect.iot;
