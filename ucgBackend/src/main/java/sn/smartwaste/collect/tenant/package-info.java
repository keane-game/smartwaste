/**
 * Module <b>Multi-tenant</b> (support).
 *
 * <p>Collectivités clientes (organisations) et cloisonnement de leurs données. Isolé de
 * {@code identity} car les deux répondent à des questions distinctes : <i>qui es-tu</i> d'un côté,
 * <i>pour le compte de quelle collectivité</i> de l'autre — un même utilisateur pouvant à terme
 * intervenir sur plusieurs territoires.
 *
 * <p>Découplage (ADR-0012) : les modules aval portent un {@code tenantId}.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Multi-tenant")
package sn.smartwaste.collect.tenant;
