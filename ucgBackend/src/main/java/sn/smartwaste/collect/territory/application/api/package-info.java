/**
 * Interface nommée <b>{@code api}</b> du module « Référentiel Territorial ».
 *
 * <p>Contrats de lecture publiés aux autres contextes. Contient notamment les <i>read-models</i> de
 * carte, produits par ce contexte et consommés par « Supervision &amp; Analytique ».
 *
 * <p>À terme, cette interface doit remplacer {@code territory.domain.repository}
 * ({@code @NamedInterface("repositories")}), concession antérieure qui expose les six repositories
 * du référentiel — cf. la note de dette qui y figure.
 */
@org.springframework.modulith.NamedInterface("api")
package sn.smartwaste.collect.territory.application.api;
