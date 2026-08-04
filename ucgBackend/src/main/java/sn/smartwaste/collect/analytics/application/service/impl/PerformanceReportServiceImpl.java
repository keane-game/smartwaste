package sn.smartwaste.collect.analytics.application.service.impl;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.analytics.application.dto.PerformanceReport;
import sn.smartwaste.collect.analytics.application.service.PerformanceReportService;
import sn.smartwaste.collect.territory.application.api.TerritoryReadModel;
import sn.smartwaste.collect.waste.application.api.CollectionPerformance;

/**
 * Compose le rapport d'efficacité et l'exporte (G5 du backlog).
 *
 * <p>Le calcul appartient au contexte « Déchets », qui possède les alertes et les passages. Ce
 * service ajoute ce que ce contexte ne peut pas savoir — le <b>nom</b> du territoire — et met le
 * résultat dans un format qu'un tableur ouvre sans rien installer.
 */
@Service
@Transactional(readOnly = true)
public class PerformanceReportServiceImpl implements PerformanceReportService {

    /** Point-virgule : le séparateur qu'attend un tableur configuré en français. */
    private static final char SEPARATEUR = ';';
    private static final String INCONNU = "non renseigne";

    private final CollectionPerformance collectionPerformance;
    private final TerritoryReadModel territory;

    public PerformanceReportServiceImpl(CollectionPerformance collectionPerformance,
                                        TerritoryReadModel territory) {
        this.collectionPerformance = collectionPerformance;
        this.territory = territory;
    }

    @Override
    public PerformanceReport reportFor(UUID communeId, Instant from, Instant to) {
        var source = collectionPerformance.reportFor(communeId, from, to);
        // Un territoire supprimé entre-temps ne doit pas emporter le rapport : ses chiffres
        // viennent des points de collecte, pas du libellé.
        // Par le port publie, jamais par le repository : `modules.verify()` a refuse une
        // premiere version qui lisait CommuneEntity depuis ce contexte, et il avait raison.
        String nom = territory.communeNameOf(communeId);
        return PerformanceReport.of(communeId, nom, from, to, source);
    }

    @Override
    public String asCsv(PerformanceReport report) {
        var csv = new StringBuilder();

        csv.append("Rapport d'efficacite de la collecte\n");
        ligne(csv, "Territoire", report.communeName() == null
                ? "Ensemble du referentiel" : report.communeName());
        ligne(csv, "Debut de periode", String.valueOf(report.from()));
        ligne(csv, "Fin de periode", String.valueOf(report.to()));
        csv.append('\n');

        ligne(csv, "Alertes levees", String.valueOf(report.alertsRaised()));
        ligne(csv, "Alertes resolues", String.valueOf(report.alertsResolved()));
        // « non renseigne » et non « 0 » : dans un tableur, un zéro se lirait comme une réactivité
        // parfaite alors qu'il signifie qu'aucune alerte n'a été refermée.
        ligne(csv, "Delai moyen de resolution (h)", report.averageResolutionHours() == null
                ? INCONNU : String.format(java.util.Locale.ROOT, "%.2f",
                        report.averageResolutionHours()));
        ligne(csv, "Points de collecte", String.valueOf(report.stops()));
        ligne(csv, "Points desservis", String.valueOf(report.served()));
        ligne(csv, "Points vides", String.valueOf(report.collected()));
        ligne(csv, "Points inaccessibles", String.valueOf(report.inaccessible()));
        ligne(csv, "Taux de realisation", String.format(java.util.Locale.ROOT, "%.2f",
                report.completionRate()));
        csv.append('\n');

        csv.append("Points les plus souvent en debordement\n");
        csv.append("depotoirId").append(SEPARATEUR)
           .append("adresse").append(SEPARATEUR)
           .append("debordements").append('\n');
        report.chronicPoints().forEach(p ->
                csv.append(p.depotoirId()).append(SEPARATEUR)
                   .append(echapper(p.address())).append(SEPARATEUR)
                   .append(p.overflowCount()).append('\n'));

        return csv.toString();
    }

    private void ligne(StringBuilder csv, String libelle, String valeur) {
        csv.append(echapper(libelle)).append(SEPARATEUR).append(echapper(valeur)).append('\n');
    }

    /**
     * Protège une valeur qui contiendrait le séparateur, un guillemet ou un saut de ligne.
     *
     * <p>Sans cela, une adresse comme « Ecole; annexe » couperait la ligne en trois et décalerait
     * toutes les colonnes suivantes : le tableur afficherait un rapport faux, sans le moindre
     * avertissement. Les adresses viennent d'un export SIG et personne ne les a nettoyées.
     */
    private static String echapper(String valeur) {
        if (valeur == null) {
            return "";
        }
        if (valeur.indexOf(SEPARATEUR) < 0 && valeur.indexOf('"') < 0
                && valeur.indexOf('\n') < 0 && valeur.indexOf('\r') < 0) {
            return valeur;
        }
        return '"' + valeur.replace("\"", "\"\"") + '"';
    }
}
