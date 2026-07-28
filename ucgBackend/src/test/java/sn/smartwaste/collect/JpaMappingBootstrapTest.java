package sn.smartwaste.collect;

import java.util.List;
import java.util.Map;

import jakarta.persistence.Entity;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Construit le <b>métamodèle Hibernate</b> de toutes les entités du projet, sans base de données.
 *
 * <p><b>Pourquoi ce test existe.</b> Une partie du câblage JPA n'est <i>que</i> des chaînes de
 * caractères : {@code mappedBy}, {@code @JoinColumn(name=…)}, les références d'attributs dans les
 * requêtes. Le compilateur ne les vérifie pas. Le seul moment où elles sont validées est la
 * construction du métamodèle, au démarrage de l'application — or celle-ci n'a jamais été lancée
 * contre une base, si bien que le « build vert » ne disait rien de la validité du mapping.
 *
 * <p>Trois {@code @OneToMany(mappedBy = "commune")} de {@code CommuneEntity} pointaient ainsi vers
 * un champ supprimé par la migration ADR-0012 (les entités du contexte « Déchets » portent
 * désormais {@code UUID communeId}). Le projet compilait, les tests passaient, et le démarrage
 * aurait échoué sur :
 * <pre>AnnotationException: Association 'CommuneEntity.depotoirs' is 'mappedBy' a property named
 * 'commune' which does not exist in the target entity 'DepotoirEntity'</pre>
 *
 * <p>Le test tourne <b>sans connexion</b> : le dialecte est fixé explicitement, ce qui dispense
 * Hibernate d'interroger la base pour le déduire. Il est donc rapide et utilisable en CI sans
 * conteneur. Il ne valide pas que le schéma SQL <i>correspond</i> aux entités (c'est le rôle de
 * {@code ddl-auto: validate} au démarrage réel, contre le schéma Liquibase) — seulement que le
 * mapping objet est cohérent avec lui-même.
 */
class JpaMappingBootstrapTest {

    /** Les deux racines coexistent pendant la migration ADR-0013. */
    private static final List<String> ENTITY_ROOT_PACKAGES =
            List.of("sn.smartwaste.collect", "sonaged.collecte.master");

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

    @Test
    @DisplayName("le métamodèle JPA se construit : tous les mappedBy et associations se résolvent")
    void jpaMetamodelBuilds() {
        List<Class<?>> entities = discoverEntities();

        // Garde-fou contre un test qui passerait à vide si le scan cessait de trouver quoi que ce
        // soit (mauvais package racine, entités déplacées) : le projet en compte une vingtaine.
        assertThat(entities)
                .as("entités découvertes dans %s", ENTITY_ROOT_PACKAGES)
                .hasSizeGreaterThan(15);

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                // Dialecte imposé : sans lui, Hibernate ouvrirait une connexion pour le déduire.
                .applySettings(Map.of(
                        AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect",
                        AvailableSettings.HBM2DDL_AUTO, "none"))
                .build();

        try {
            MetadataSources sources = new MetadataSources(registry);
            entities.forEach(sources::addAnnotatedClass);

            // C'est ici que tout se joue : buildMetadata() lève sur un mappedBy orphelin, un
            // type d'identifiant incohérent, une @JoinColumn en double, etc.
            Metadata metadata = sources.buildMetadata();

            assertThat(metadata.getEntityBindings())
                    .as("liaisons d'entités produites par Hibernate")
                    .hasSize(entities.size());
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
