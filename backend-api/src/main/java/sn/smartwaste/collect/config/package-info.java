/**
 * Module <b>Configuration</b> — <b>non</b> un contexte borné (ADR-0013).
 *
 * <p>Amorçage applicatif et technique transverse : {@code SonagedApplication}, la configuration
 * MinIO et OpenAPI, les annotations maison, les aspects AOP et les gestionnaires d'exception
 * globaux ({@code @ControllerAdvice}).
 *
 * <p>Rien de métier ici : ce module ne doit contenir que ce qui sert à démarrer et à câbler
 * l'application. Il ne publie aucune interface — personne n'est censé en dépendre.
 *
 * <p><b>Deux vestiges connus</b>, conservés faute de validation pour les retirer :
 * <ul>
 *   <li>{@code DataNotifierAspect} — son pointcut vise
 *       {@code com.worldline.tapandgo.user.annotations.Notifiable}, un projet <b>étranger</b>.
 *       Il ne peut donc jamais intercepter le {@code @Notifiable} local : l'aspect est mort.</li>
 *   <li>{@code SleuthTraceJmsListener} — Spring Cloud Sleuth n'est pas une dépendance du projet.</li>
 * </ul>
 */
@org.springframework.modulith.ApplicationModule(displayName = "Configuration")
package sn.smartwaste.collect.config;
