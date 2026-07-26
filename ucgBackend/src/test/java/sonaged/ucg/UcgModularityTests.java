package sonaged.ucg;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Tests de modularité du monolithe modulaire cible (Spring Modulith).
 *
 * <p>Analyse la structure enracinée sur {@link UcgModulith} :
 * <ul>
 *   <li>{@code verifiesModularStructure} — vérifie l'absence de cycles entre modules et le respect
 *       des frontières (aucun accès aux internals d'un autre module).</li>
 *   <li>{@code writesDocumentation} — génère les diagrammes PlantUML + le tableau des modules
 *       sous {@code target/spring-modulith-docs/}.</li>
 * </ul>
 *
 * <p>NB : {@code maven-surefire-plugin} a {@code skipTests=true} dans le pom ; lancer explicitement via
 * {@code ./mvnw test -DskipTests=false -Dtest=UcgModularityTests}.
 */
class UcgModularityTests {

    private final ApplicationModules modules = ApplicationModules.of(UcgModulith.class);

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
