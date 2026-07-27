/**
 * Module <b>Communication</b> (support, en aval).
 *
 * <p>Signalements citoyens ({@code Avis}) et notifications multi-canal <b>e-mail / SSE temps réel /
 * push FCM</b>. Consomme {@code AlertRaised}, {@code UserRegistered} ; publie {@code AvisSubmitted},
 * {@code NotificationSent}.
 *
 * <p>Découplage (ADR-0012) : référence {@code userId} / {@code depotoirId} par identifiant.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Communication")
package sonaged.ucg.communication;

