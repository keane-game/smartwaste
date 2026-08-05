package sn.smartwaste.collect.platform.presentation.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import sn.smartwaste.collect.platform.application.service.AvisService;
import sn.smartwaste.collect.platform.domain.model.Avis;
import sn.smartwaste.collect.platform.domain.model.AvisStatus;

/**
 * Signalements citoyens (`/avis`).
 *
 * <p>Le mémoire fait du signalement de dépôt sauvage un cas d'usage citoyen explicite. Jusqu'ici
 * l'API ne savait qu'<b>enregistrer</b> un avis : ni le localiser, ni le suivre, ni le clore. Un
 * signalement qu'on ne peut pas traiter n'est pas un signalement, c'est une boîte aux lettres.
 *
 * <p><b>Deux publics, deux niveaux d'accès.</b> Déposer un signalement et consulter les siens sont
 * des gestes d'habitant : tout compte authentifié y a droit. Consulter la file de traitement et
 * faire avancer un signalement sont des gestes d'exploitation, réservés à l'encadrement.
 * Sans cette séparation, n'importe quel habitant pouvait clore ou rejeter le signalement d'autrui,
 * et lire l'intégralité des signalements de la ville.
 *
 * <p>Les rôles cités sont ceux qui <b>existent réellement</b> (semés par le changelog 2.1.0) :
 * {@code SUPER_ADMIN}, {@code ADMIN}, {@code USER}. Le modèle ne connaît ni agent ni superviseur —
 * les modéliser est une évolution distincte, qui rendra cette règle plus fine.
 */
@AllArgsConstructor
@RequestMapping("avis")
@RestController
public class AvisControleur {

    private final AvisService avisService;

    @Operation(summary = "Déposer un signalement (dépôt sauvage, nuisance…)")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void create(@RequestBody Avis avis) {
        this.avisService.create(avis);
    }

    @Operation(summary = "Mes signalements")
    @GetMapping("/mine")
    public List<Avis> mine() {
        return avisService.mine();
    }

    @Operation(summary = "File de traitement : signalements dans un état donné")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public List<Avis> byStatus(@RequestParam(value = "statut", defaultValue = "SIGNALE") AvisStatus statut) {
        return avisService.byStatus(statut);
    }

    @Operation(summary = "Signalements ouverts et localisés, pour la carte de supervision")
    @GetMapping("/map")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public List<Avis> forMap() {
        return avisService.openWithLocation();
    }

    @Operation(summary = "Faire avancer un signalement dans son cycle de vie",
               description = "SIGNALE -> EN_COURS -> TRAITE|REJETE. Une transition interdite rend 409.")
    @PutMapping("/{avisId}/statut/{statut}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public Avis changeStatus(@PathVariable("avisId") int avisId,
                             @PathVariable("statut") AvisStatus statut) {
        return avisService.changeStatus(avisId, statut);
    }
}
