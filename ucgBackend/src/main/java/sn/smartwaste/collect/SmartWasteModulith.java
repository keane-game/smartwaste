package sn.smartwaste.collect;

import org.springframework.modulith.Modulithic;

/**
 * Ancre du monolithe modulaire DDD <b>SmartWaste Collect</b> (Spring Modulith).
 *
 * <p>Ce type <b>n'est pas</b> une application Spring Boot : il ne porte pas de {@code main} et ne
 * remplace pas {@code sn.smartwaste.collect.config.SonagedApplication}. Il sert uniquement de point
 * d'entrée à l'analyse de modularité ({@code ApplicationModules.of(SmartWasteModulith.class)}),
 * afin de matérialiser et vérifier les frontières des contextes bornés.
 *
 * <p>Trois racines cohabitent pendant la transition : le code hérité
 * ({@code sonaged.collecte.master}, organisé par couche technique), le squelette intermédiaire
 * ({@code sonaged.ucg}, ancré sur {@code UcgModulith}) et cette racine cible. Chaque module migré
 * quitte définitivement les deux premières pour arriver ici ; les deux ancres restent donc
 * vérifiées en parallèle tant que la migration n'est pas terminée.
 *
 * <p>Chaque module est découpé en couches (Clean Architecture) :
 * {@code presentation} → {@code application} → {@code domain} → {@code infrastructure},
 * les dépendances ne pointant que vers l'intérieur.
 *
 * <p>Modules :
 * <ul>
 *   <li>{@code identity}  — Identité &amp; Accès (authentification, utilisateurs, rôles)</li>
 *   <li>{@code tenant}    — Multi-tenant (collectivités clientes et leur cloisonnement)</li>
 *   <li>{@code territory} — Référentiel territorial (région, département, commune, quartier)</li>
 *   <li>{@code waste}     — Cœur métier déchets (points de collecte, alertes, circuits)</li>
 *   <li>{@code iot}       — Ingestion IoT (RÉSERVÉ — non implémenté)</li>
 *   <li>{@code platform}  — Services de plateforme (avis citoyens, notifications, fichiers)</li>
 *   <li>{@code analytics} — Supervision (tableaux de bord, statistiques, projections)</li>
 *   <li>{@code shared}    — Shared kernel (module ouvert)</li>
 *   <li>{@code config}    — Configuration technique transverse (hors périmètre métier)</li>
 * </ul>
 */
@Modulithic(systemName = "SmartWaste Collect")
public final class SmartWasteModulith {

    private SmartWasteModulith() {
        // ancre de modularité — pas d'instanciation
    }
}
