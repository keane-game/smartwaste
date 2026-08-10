package sn.smartwaste.collect.tenant.application.api;

import java.util.Optional;
import java.util.UUID;

/**
 * Collectivité pour le compte de laquelle la requête en cours est traitée.
 *
 * <p>C'est la brique qu'ADR-0013 §4 demandait de poser <i>avant</i> d'être exploitée. ADR-0020 en
 * fait le premier usage réel : les agrégats métier portent désormais {@code organizationId}.
 */
public interface CurrentTenantProvider {

    /**
     * Collectivité de démarrage (« Ville de Pikine »), seule active à ce jour (ADR-0020).
     *
     * <p>Référence pour les imports systèmes qui n'ont pas d'utilisateur authentifié à interroger
     * (ex. {@code GeoJsonImportRunner} au démarrage) — les jeux de données importés (`datas/`) ne
     * décrivent d'ailleurs que le territoire de Pikine. <b>Pas</b> une valeur par défaut à utiliser
     * pour un chemin d'écriture authentifié : là, une organisation absente doit échouer, pas se
     * replier silencieusement sur celle-ci.
     */
    UUID PIKINE_ORGANIZATION_ID = UUID.fromString("01983c4f-0001-7000-8000-000000000001");

    /**
     * @return la collectivité de l'utilisateur authentifié, ou {@link Optional#empty()} si la
     *         requête n'est pas authentifiée ou si l'utilisateur n'est rattaché à aucune
     *         collectivité. <b>Un {@code Optional} vide n'autorise rien</b> : l'appelant qui
     *         cloisonne doit refuser, jamais élargir au global.
     */
    Optional<UUID> currentOrganizationId();
}
