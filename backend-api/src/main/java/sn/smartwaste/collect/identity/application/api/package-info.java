/**
 * Interface nommée <b>{@code api}</b> du module « Identité &amp; Accès ».
 *
 * <p>Seul point d'entrée légal des autres contextes vers l'identité. Tout le reste du module
 * (entités, repositories, sécurité, contrôleurs) est <i>interne</i> au sens de Spring Modulith :
 * un accès direct fait échouer {@code modules.verify()}.
 *
 * <p><b>Pourquoi un port applicatif et non les repositories.</b> L'ADR-0013 §3 interdit
 * explicitement l'accès au repository d'un autre contexte. Les contrats publiés ici n'exposent
 * que des types autonomes ({@code UUID}) : aucun autre module ne voit {@code UserEntity}, et
 * l'externalisation de l'identité vers Keycloak (ADR-0011) se fera en réimplémentant ce port,
 * sans toucher aux appelants.
 *
 * <p><i>(Le référentiel territorial publie encore ses repositories via
 * {@code territory.domain.repository} — concession antérieure à convertir sur ce modèle.)</i>
 */
@org.springframework.modulith.NamedInterface("api")
package sn.smartwaste.collect.identity.application.api;
