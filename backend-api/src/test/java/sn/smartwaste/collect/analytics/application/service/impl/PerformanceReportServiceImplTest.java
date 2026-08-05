package sn.smartwaste.collect.analytics.application.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.smartwaste.collect.territory.application.api.TerritoryReadModel;
import sn.smartwaste.collect.waste.application.api.CollectionPerformance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Composition et export du rapport d'efficacité (G5 du backlog).
 *
 * <p>Le calcul appartient au contexte « Déchets » et se vérifie ailleurs. Ce qui se joue ici est ce
 * que {@code analytics} ajoute : <b>nommer</b> le territoire — un rapport qui dirait « commune
 * 019fb3cc-78fb… » ne serait lu par personne — et rendre le tout exploitable dans un tableur.
 */
@ExtendWith(MockitoExtension.class)
class PerformanceReportServiceImplTest {

    private static final UUID COMMUNE = UUID.randomUUID();
    private static final Instant DEBUT = Instant.parse("2026-07-01T00:00:00Z");
    private static final Instant FIN = Instant.parse("2026-08-01T00:00:00Z");

    @Mock private CollectionPerformance collectionPerformance;
    @Mock private TerritoryReadModel territory;

    private PerformanceReportServiceImpl service() {
        return new PerformanceReportServiceImpl(collectionPerformance, territory);
    }

    private void performanceReturns(CollectionPerformance.PerformanceReport report) {
        lenient().when(collectionPerformance.reportFor(any(), any(), any())).thenReturn(report);
    }

    private CollectionPerformance.PerformanceReport source(
            List<CollectionPerformance.ProblemPoint> chronic) {
        return new CollectionPerformance.PerformanceReport(12, 9, 3.5, 24, 18, 15, 3, chronic);
    }

    private void communeIsNamed(String name) {
        when(territory.communeNameOf(COMMUNE)).thenReturn(name);
    }

    @Test
    @DisplayName("le rapport porte le nom du territoire, pas son identifiant")
    void namesTheTerritory() {
        performanceReturns(source(List.of()));
        communeIsNamed("mbao");

        var report = service().reportFor(COMMUNE, DEBUT, FIN);

        assertThat(report.communeName()).isEqualTo("mbao");
        assertThat(report.stops()).isEqualTo(24);
        assertThat(report.completionRate()).isEqualTo(18d / 24d);
    }

    @Test
    @DisplayName("sans commune, le rapport couvre tout le referentiel et le dit")
    void wholeTerritoryHasNoName() {
        performanceReturns(source(List.of()));

        var report = service().reportFor(null, DEBUT, FIN);

        assertThat(report.communeId()).isNull();
        assertThat(report.communeName()).isNull();
    }

    @Test
    @DisplayName("une commune supprimee entre-temps ne fait pas echouer le rapport")
    void missingCommuneDoesNotBreakTheReport() {
        // Le rapport reste juste : ses chiffres viennent des points, pas du libelle.
        performanceReturns(source(List.of()));
        when(territory.communeNameOf(COMMUNE)).thenReturn(null);

        var report = service().reportFor(COMMUNE, DEBUT, FIN);

        assertThat(report.communeName()).isNull();
        assertThat(report.stops()).isEqualTo(24);
    }

    @Test
    @DisplayName("le CSV porte les indicateurs puis le tableau des points chroniques")
    void csvCarriesBothSections() {
        performanceReturns(source(List.of(
                new CollectionPerformance.ProblemPoint(76L, "Zac Mbao", 5))));
        communeIsNamed("mbao");

        String csv = service().asCsv(service().reportFor(COMMUNE, DEBUT, FIN));

        assertThat(csv).contains("Points de collecte;24");
        assertThat(csv).contains("Points desservis;18");
        assertThat(csv).contains("depotoirId;adresse;debordements");
        assertThat(csv).contains("76;Zac Mbao;5");
    }

    @Test
    @DisplayName("une adresse contenant le separateur ne decale pas les colonnes")
    void csvEscapesTheSeparator() {
        // « Ecole; annexe » couperait la ligne en trois et decalerait toutes les colonnes
        // suivantes : le tableur afficherait un rapport faux sans le moindre avertissement.
        performanceReturns(source(List.of(
                new CollectionPerformance.ProblemPoint(76L, "Ecole; annexe \"nord\"", 5))));
        communeIsNamed("mbao");

        String csv = service().asCsv(service().reportFor(COMMUNE, DEBUT, FIN));

        assertThat(csv).contains("\"Ecole; annexe \"\"nord\"\"\"");
        // La ligne du point conserve exactement trois colonnes.
        String ligne = csv.lines().filter(l -> l.startsWith("76;")).findFirst().orElseThrow();
        assertThat(compterColonnes(ligne)).isEqualTo(3);
    }

    @Test
    @DisplayName("un delai inconnu s'ecrit comme tel, jamais comme zero")
    void csvDistinguishesUnknownFromZero() {
        // Zero se lirait comme une reactivite parfaite dans un tableur.
        performanceReturns(new CollectionPerformance.PerformanceReport(
                3, 0, null, 24, 0, 0, 0, List.of()));
        communeIsNamed("mbao");

        String csv = service().asCsv(service().reportFor(COMMUNE, DEBUT, FIN));

        assertThat(csv).contains("Delai moyen de resolution (h);non renseigne");
        assertThat(csv).doesNotContain("Delai moyen de resolution (h);0");
    }

    /** Compte les colonnes en respectant les guillemets, comme le ferait un tableur. */
    private static int compterColonnes(String ligne) {
        int colonnes = 1;
        boolean dansGuillemets = false;
        for (char c : ligne.toCharArray()) {
            if (c == '"') {
                dansGuillemets = !dansGuillemets;
            } else if (c == ';' && !dansGuillemets) {
                colonnes++;
            }
        }
        return colonnes;
    }
}
