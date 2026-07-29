package sn.smartwaste.collect;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Tests de modularité du monolithe modulaire DDD (Spring Modulith).
 *
 * <p>Analyse la structure enracinée sur {@link SmartWasteModulith} :
 * <ul>
 *   <li>{@code verifiesModularStructure} — vérifie l'absence de cycles entre modules et le respect
 *       des frontières (aucun accès aux internals d'un autre module).</li>
 *   <li>{@code writesDocumentation} — génère les diagrammes PlantUML + le tableau des modules
 *       sous {@code target/spring-modulith-docs/}.</li>
 * </ul>
 *
 * <p>C'est désormais la <b>seule</b> ancre de modularité du projet : {@code sonaged.ucg} et son
 * {@code UcgModularityTests} — qui ne vérifiaient plus rien depuis l'ADR-0013 — ont été supprimés.
 *
 * <p>NB : {@code maven-surefire-plugin} a {@code skipTests=true} dans le pom ; lancer explicitement via
 * {@code ./mvnw test -DskipTests=false -Dtest=SmartWasteModularityTests}.
 */
class SmartWasteModularityTests {

    private final ApplicationModules modules = ApplicationModules.of(SmartWasteModulith.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void writesDocumentation() {
        new Documenter(modules)
                .writeDocumentation()
                .writeIndividualModulesAsPlantUml();
    }

    @Test
    void printModules() {
        modules.forEach(System.out::println);
    }
}
