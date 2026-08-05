/**
 * Interface nommée <b>{@code api}</b> du module « Multi-tenant ».
 *
 * <p>Seul point d'entrée légal des autres contextes vers le cloisonnement par collectivité.
 * Organisations, rattachements et repositories restent internes : les appelants ne manipulent
 * qu'un {@code UUID} d'organisation.
 */
@org.springframework.modulith.NamedInterface("api")
package sn.smartwaste.collect.tenant.application.api;
