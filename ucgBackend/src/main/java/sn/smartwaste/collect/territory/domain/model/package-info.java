/**
 * Interface nommée <b>{@code model}</b> — ⚠️ <b>CONCESSION TRANSITOIRE</b>.
 *
 * <p>Exposée pour une seule raison : l'<b>import GeoJSON</b> ({@code administration}) écrit
 * directement dans ces types. C'est contraire à l'ADR-0013 §3, et assumé comme dette : le
 * recâbler sur les services applicatifs de chaque contexte est un refactoring de ~550 lignes,
 * traité séparément. Le jour où il est fait, <b>ce fichier disparaît</b> et la frontière se
 * referme d'elle-même.
 *
 * <p>Aucun autre module ne doit s'en servir : les consommateurs légitimes passent par les ports
 * publiés ({@code *.application.api}).
 */
@org.springframework.modulith.NamedInterface("model")
package sn.smartwaste.collect.territory.domain.model;
