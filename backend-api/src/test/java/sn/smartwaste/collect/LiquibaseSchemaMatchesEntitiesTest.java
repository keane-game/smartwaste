package sn.smartwaste.collect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.persistence.Entity;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.yaml.snakeyaml.Yaml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Confronte les <b>colonnes attendues par les entités</b> au <b>schéma réellement en base</b>.
 *
 * <p><b>Le trou que ce test ferme.</b> Les deux tests de démarrage existants se partagent le travail
 * de telle sorte que personne ne faisait celui-ci : {@link JpaMappingBootstrapTest} vérifie que le
 * mapping objet est cohérent <i>avec lui-même</i>, et {@code ApplicationContextLoadsTest} démarre le
 * contexte sur un schéma <i>dérivé des entités</i> ({@code create-drop}, Liquibase désactivé), donc
 * conforme par construction. Chacun documente honnêtement sa limite et renvoie la vérification à
 * {@code ddl-auto: validate} au premier démarrage réel. Ce démarrage a eu lieu, et il a échoué :
 * <pre>SchemaManagementException: missing column [Address] in table [ALERT]</pre>
 * Quatre colonnes manquaient en réalité (cf. {@code 2.10.0_schema_validate_gaps.xml}).
 *
 * <p><b>Ce qu'il apporte par rapport à {@code ddl-auto: validate}.</b> Le validateur d'Hibernate
 * s'arrête à la <i>première</i> anomalie : il faut un redémarrage par colonne manquante pour
 * découvrir les suivantes. Ici la comparaison est faite en entier et l'échec les liste toutes.
 *
 * <p><b>Pourquoi il exige une vraie base.</b> H2 a été essayé et écarté : Liquibase y traduit le
 * {@code dropPrimaryKey} de {@code 2.0.0_territory_uuid.xml} en {@code ALTER TABLE … DROP PRIMARY
 * KEY}, que H2 refuse quand la clé porte un nom de contrainte. Corriger le changeset pour arranger
 * le test n'est pas possible : il est déjà appliqué, sa somme de contrôle est figée.
 *
 * <p><b>Sans base joignable, le test est neutralisé plutôt que faux.</b> C'est la contrepartie
 * assumée : il protège le poste de développement et tout environnement disposant de la base, pas une
 * CI sans PostgreSQL.
 *
 * <p><b>Ce qu'il ne couvre pas.</b> Les types SQL, les contraintes et les colonnes présentes en base
 * mais non mappées (que {@code validate} ignore également). Il couvre l'existence des tables et des
 * colonnes, qui est la classe d'écart constatée.
 */
class LiquibaseSchemaMatchesEntitiesTest {

    /** Les deux racines coexistent pendant la migration ADR-0013. */
    private static final List<String> ENTITY_ROOT_PACKAGES =
            List.of("sn.smartwaste.collect", "sonaged.collecte.master");

    /**
     * Les coordonnées de connexion sont <b>lues dans {@code application.yml}</b>, placeholders
     * {@code ${VAR:défaut}} résolus comme le ferait Spring. Ce détour évite de recopier ici le mot
     * de passe : le dépôt en contient déjà un exemplaire de trop (ADR-0002 §4-5, rotation jamais
     * faite), il n'y a pas de raison d'en ajouter un second qu'il faudrait penser à purger aussi.
     */
    private static final Map<String, String> DATASOURCE = readDatasourceConfig();

    @SuppressWarnings("unchecked")
    private static Map<String, String> readDatasourceConfig() {
        try (var stream = LiquibaseSchemaMatchesEntitiesTest.class
                .getClassLoader().getResourceAsStream("application.yml")) {
            Map<String, Object> root = new Yaml().load(stream);
            Map<String, Object> datasource =
                    (Map<String, Object>) ((Map<String, Object>) root.get("spring"))
                            .get("datasource");
            return Map.of(
                    "url", resolve(datasource.get("url")),
                    "username", resolve(datasource.get("username")),
                    "password", resolve(datasource.get("password")));
        } catch (Exception e) {
            throw new IllegalStateException("Configuration datasource illisible", e);
        }
    }

    /** Résout {@code ${VARIABLE:valeur par défaut}} contre l'environnement. */
    private static String resolve(Object rawValue) {
        String raw = String.valueOf(rawValue);
        Matcher placeholder = Pattern.compile("^\\$\\{([^:}]+):?([^}]*)}$").matcher(raw);
        if (!placeholder.matches()) {
            return raw;
        }
        String fromEnvironment = System.getenv(placeholder.group(1));
        return fromEnvironment == null || fromEnvironment.isBlank()
                ? placeholder.group(2)
                : fromEnvironment;
    }

    @Test
    @DisplayName("toute colonne mappée par une entité existe dans le schéma")
    void everyMappedColumnExistsInTheSchema() throws Exception {
        Map<String, Set<String>> actual;
        try (Connection connection = openConnectionOrSkip()) {
            actual = actualSchemaFrom(connection);
        }

        Map<String, Set<String>> expected = expectedSchemaFromEntities();

        // Garde-fous contre une comparaison qui passerait à vide : scan d'entités cassé d'un côté,
        // base vide ou schéma inattendu de l'autre. Le projet compte une vingtaine d'entités.
        assertThat(expected).as("tables attendues par les entités").hasSizeGreaterThan(15);
        assertThat(actual).as("tables présentes en base").hasSizeGreaterThan(15);

        List<String> anomalies = new ArrayList<>();
        expected.forEach((table, columns) -> {
            Set<String> present = actual.get(table);
            if (present == null) {
                anomalies.add("table absente : " + table
                        + " (attendue avec " + columns.size() + " colonnes)");
                return;
            }
            Set<String> missing = new TreeSet<>(columns);
            missing.removeAll(present);
            missing.forEach(column -> anomalies.add("colonne absente : " + table + "." + column));
        });

        assertThat(anomalies)
                .as("écarts entités JPA ↔ schéma — chacun fait échouer `ddl-auto: validate` "
                        + "au démarrage, un redémarrage à la fois")
                .isEmpty();
    }

    private static Connection openConnectionOrSkip() {
        try {
            return DriverManager.getConnection(DATASOURCE.get("url"),
                    DATASOURCE.get("username"), DATASOURCE.get("password"));
        } catch (Exception unreachable) {
            assumeTrue(false, "Base indisponible (" + DATASOURCE.get("url") + ") : "
                    + "vérification entités ↔ schéma non exécutée — " + unreachable.getMessage());
            throw new IllegalStateException("inatteignable", unreachable);
        }
    }

    /** Colonnes que chaque entité exige, telles qu'Hibernate les résoudra au démarrage. */
    private static Map<String, Set<String>> expectedSchemaFromEntities() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySettings(Map.of(
                        AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect",
                        AvailableSettings.HBM2DDL_AUTO, "none",
                        // Mêmes stratégies qu'en production : sans elles, `AlertId` deviendrait
                        // `alert_id` et la comparaison porterait sur d'autres noms que la réalité.
                        AvailableSettings.PHYSICAL_NAMING_STRATEGY,
                        "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl",
                        AvailableSettings.IMPLICIT_NAMING_STRATEGY,
                        "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy"))
                .build();

        try {
            MetadataSources sources = new MetadataSources(registry);
            discoverEntities().forEach(sources::addAnnotatedClass);
            Metadata metadata = sources.buildMetadata();

            Map<String, Set<String>> schema = new TreeMap<>();
            for (var namespace : metadata.getDatabase().getNamespaces()) {
                for (Table table : namespace.getTables()) {
                    if (!table.isPhysicalTable()) {
                        continue;
                    }
                    Set<String> columns =
                            schema.computeIfAbsent(normalize(table.getName()), k -> new TreeSet<>());
                    for (Column column : table.getColumns()) {
                        columns.add(normalize(column.getName()));
                    }
                }
            }
            return schema;
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private static Map<String, Set<String>> actualSchemaFrom(Connection connection)
            throws Exception {
        Map<String, Set<String>> schema = new LinkedHashMap<>();
        DatabaseMetaData meta = connection.getMetaData();

        try (ResultSet tables = meta.getTables(null, "public", "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                schema.put(normalize(tables.getString("TABLE_NAME")), new LinkedHashSet<>());
            }
        }

        try (ResultSet columns = meta.getColumns(null, "public", "%", "%")) {
            while (columns.next()) {
                Set<String> target = schema.get(normalize(columns.getString("TABLE_NAME")));
                if (target != null) {
                    target.add(normalize(columns.getString("COLUMN_NAME")));
                }
            }
        }
        return schema;
    }

    /**
     * PostgreSQL replie les identifiants non quotés en minuscules : c'est cette règle, et non une
     * commodité de test, qui autorise à comparer {@code Address} et {@code address}.
     */
    private static String normalize(String identifier) {
        return identifier.toLowerCase(Locale.ROOT);
    }

    private static List<Class<?>> discoverEntities() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        return ENTITY_ROOT_PACKAGES.stream()
                .flatMap(root -> scanner.findCandidateComponents(root).stream())
                .map(BeanDefinition::getBeanClassName)
                .distinct()
                .<Class<?>>map(name -> {
                    try {
                        return Class.forName(name);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException("Entité introuvable : " + name, e);
                    }
                })
                .toList();
    }
}
