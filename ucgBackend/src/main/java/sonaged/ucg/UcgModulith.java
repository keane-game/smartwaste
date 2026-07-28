package sonaged.ucg;

import org.springframework.modulith.Modulithic;

/**
 * Ancre du monolithe modulaire cible (Spring Modulith).
 *
 * <p>Ce type <b>n'est pas</b> une application Spring Boot : il ne porte pas de {@code main} et ne
 * remplace pas {@code sn.smartwaste.collect.config.SonagedApplication}. Il sert uniquement de point
 * d'entrée à l'analyse de modularité ({@code ApplicationModules.of(UcgModulith.class)}), afin de
 * matérialiser et vérifier les frontières des modules décrits dans {@code docs/architecture-cible.md}.
 *
 * <p><b>Arborescence en voie de retrait.</b> La cible est désormais {@code sn.smartwaste.collect}
 * (ancre {@code SmartWasteModulith}), qui ajoute un découpage en couches Clean Architecture. Les
 * modules {@code communication} et {@code supervision} y ont déjà été migrés — ils ne figurent
 * plus ci-dessous. Cette ancre reste vérifiée tant que des modules subsistent ici ; elle
 * disparaîtra avec le dernier d'entre eux.
 *
 * <p>Modules restants :
 * <ul>
 *   <li>{@code identiteacces} — Identité &amp; Accès (Keycloak + profils)</li>
 *   <li>{@code referentiel}   — Référentiel territorial</li>
 *   <li>{@code collecte}      — Gestion des collectes (points, alertes, circuits)</li>
 *   <li>{@code ingestioniot}  — Ingestion IoT (RÉSERVÉ — non implémenté)</li>
 *   <li>{@code shared}        — Shared kernel (module ouvert)</li>
 * </ul>
 */
@Modulithic(systemName = "UCG / SONAGED")
public final class UcgModulith {

    private UcgModulith() {
        // ancre de modularité — pas d'instanciation
    }
}
