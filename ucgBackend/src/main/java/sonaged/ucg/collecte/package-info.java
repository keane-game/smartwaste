/**
 * Module <b>Gestion des Collectes</b> (cœur opérationnel).
 *
 * <p>Organisé en sous-domaines cloisonnés (packages internes, tables séparées, communication par
 * id/événements) :
 * <ul>
 *   <li>{@code pointscollecte} — Dépotoirs, types, mobilier urbain, registre des capteurs
 *       (+ {@code fillLevel} dénormalisé). Consomme {@code MeasurementRecorded}.</li>
 *   <li>{@code alertes} — Alertes (auto sur seuil + manuelles), photos. Consomme
 *       {@code FillThresholdExceeded} ; publie {@code AlertRaised}.</li>
 *   <li>{@code circuitstournees} — Circuits collecte/balayage, shifts, tournées (optimisation future).</li>
 * </ul>
 *
 * <p>L'ingestion IoT (fort volume) est un <b>module séparé</b> {@code ingestioniot}, pas un sous-domaine
 * d'ici, pour pouvoir scaler/extraire indépendamment.
 *
 * <p>Découplage (ADR-0012) : référence {@code communeId}, {@code quartierId}, {@code depotoirId},
 * {@code userId} par identifiant.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Gestion des Collectes")
package sonaged.ucg.collecte;
