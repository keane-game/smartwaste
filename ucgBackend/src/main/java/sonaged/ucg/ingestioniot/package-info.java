/**
 * Module <b>Ingestion IoT</b> — ⚠️ <b>RÉSERVÉ / NON IMPLÉMENTÉ</b>.
 *
 * <p>La brique IoT (capteurs de niveau de remplissage, ingestion des mesures, moteur de seuils)
 * <b>n'existe pas encore</b> dans le produit. Ce module est déclaré <b>uniquement pour réserver la
 * frontière et la couture d'extraction</b> (fort volume → premier microservice à extraire, ADR-0010).
 *
 * <p>Il ne contient <b>aucun composant Spring, aucun endpoint, aucune entité</b> : ce n'est pas un
 * point actif du système en cours d'exécution. À ne peupler que lorsque la fonctionnalité IoT sera
 * décidée (ROADMAP P0-5/P0-6) — entité cible {@code MeasurementEntity}, endpoint {@code POST /v1/measurements},
 * événements {@code MeasurementRecorded} / {@code FillThresholdExceeded}.
 *
 * <p>Découplage (ADR-0012) : référencera {@code depotoirId} / {@code capteurId} par identifiant.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Ingestion IoT (réservé)")
package sonaged.ucg.ingestioniot;
