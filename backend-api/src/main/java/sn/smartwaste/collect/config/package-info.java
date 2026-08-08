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
 * <p>{@code DataNotifierAspect} (pointcut mort visant un projet étranger,
 * {@code com.worldline.tapandgo}), l'annotation {@code @Notifiable} qu'il ciblait, et
 * {@code SleuthTraceJmsListener} (Spring Cloud Sleuth n'est pas une dépendance du projet) ont été
 * retirés le 2026-08-08 — validation explicite obtenue, zéro lecteur en dehors d'eux-mêmes.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Configuration")
package sn.smartwaste.collect.config;
