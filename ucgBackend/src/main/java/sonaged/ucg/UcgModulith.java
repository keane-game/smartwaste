package sonaged.ucg;

import org.springframework.modulith.Modulithic;

/**
 * Ancre du monolithe modulaire cible (Spring Modulith).
 *
 * <p>Ce type <b>n'est pas</b> une application Spring Boot : il ne porte pas de {@code main} et ne
 * remplace pas {@code sonaged.collecte.master.SonagedApplication}. Il sert uniquement de point
 * d'entrée à l'analyse de modularité ({@code ApplicationModules.of(UcgModulith.class)}), afin de
 * matérialiser et vérifier les frontières des modules décrits dans {@code docs/architecture-cible.md}.
 *
 * <p>Le code métier existant (package {@code sonaged.collecte.master}) sera migré progressivement,
 * module par module, dans les sous-packages de {@code sonaged.ucg} (tâche P1-7 de la ROADMAP).
 *
 * <p>Modules :
 * <ul>
 *   <li>{@code identiteacces} — Identité &amp; Accès (Keycloak + profils)</li>
 *   <li>{@code referentiel}   — Référentiel territorial</li>
 *   <li>{@code collecte}      — Gestion des collectes (points, alertes, circuits)</li>
 *   <li>{@code ingestioniot}  — Ingestion IoT (RÉSERVÉ — non implémenté)</li>
 *   <li>{@code communication} — Avis + notifications</li>
 *   <li>{@code supervision}   — Dashboard, statistiques, historique/audit</li>
 *   <li>{@code shared}        — Shared kernel (module ouvert)</li>
 * </ul>
 */
@Modulithic(systemName = "UCG / SONAGED")
public final class UcgModulith {

    private UcgModulith() {
        // ancre de modularité — pas d'instanciation
    }
}
