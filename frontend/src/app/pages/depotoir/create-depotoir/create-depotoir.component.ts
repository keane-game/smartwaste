import { Component } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { SharedService } from '../../../services/shared.service';
import { first } from 'rxjs';
import { succesAlert, errorAlert } from '../../../services/alert.service';
import { API_ENDPOINTS } from '../../../shared/constants/api-endpoints';

/**
 * Création / modification d'un point de collecte.
 *
 * <p><b>Les noms de champs ne correspondaient à rien.</b> Le formulaire envoyait
 * {@code depotoirAddress}, {@code quartier}, {@code longitude} et {@code latitude}, alors que le
 * DTO `Depotoir` attend {@code address}, {@code quartierId} et une liste {@code coordinates} —
 * seul {@code typeDepotoir} coïncidait. Le serveur acceptait la requête et enregistrait un point
 * sans adresse ni quartier : l'écran paraissait fonctionner, les données étaient vides. Même
 * classe de défaut que sur les formulaires Commune et Quartier.
 *
 * <p>Les listes déroulantes passent d'un `ng-multiselect-dropdown` à des `select` simples, comme
 * les autres dialogues : le composant rendait des tableaux, ce que le DTO n'attend pas.
 *
 * <p><b>Pas de saisie de latitude/longitude.</b> Le DTO expose bien une liste {@code coordinates},
 * mais elle n'est câblée dans aucun sens : {@code DepotoirServiceImpl.createDepotoir} ne la lit
 * pas (la position est portée par {@code geometry}) et elle ressort vide sur les 72 points en base,
 * import GeoJSON compris. Deux champs qui n'écrivent ni ne relisent rien seraient exactement le
 * défaut corrigé plus haut ; ils sont remplacés par une mention de ce que l'écran ne fait pas.
 */
@Component({
    selector: 'app-create-depotoir',
    templateUrl: './create-depotoir.component.html',
    styleUrls: ['./create-depotoir.component.scss'],
    standalone: false
})
export class CreateDepotoirComponent {

  submitted = false;
  saving = false;
  depotoirForm!: FormGroup;
  currentDepotoir: any;
  id: any;

  listTypes: any[] = [];
  listQuartiers: any[] = [];

  /** Chargement des menus déroulants — voir le commentaire de `ngOnInit` sur la lenteur des quartiers. */
  loadingTypes = true;
  loadingQuartiers = true;

  constructor(
    private createDepotoirModal: MatDialogRef<CreateDepotoirComponent>,
    private formBuilder: FormBuilder,
    private sharedService: SharedService,
  ) { }

  get isEdit(): boolean {
    return this.id != undefined;
  }

  ngOnInit(): void {
    this.depotoirForm = this.formBuilder.group({
      address: ['', Validators.required],
      typeDepotoirId: [null, Validators.required],
      quartierId: [null, Validators.required],
    });

    if (this.isEdit) {
      this.depotoirForm.patchValue({
        address: this.currentDepotoir?.address ?? '',
        typeDepotoirId: this.currentDepotoir?.typeDepotoir?.typeDepotoirId ?? null,
        quartierId: this.currentDepotoir?.quartierId ?? null,
      });
    }

    // `listPath` (`/s`) : liste complète pour peupler les menus — le chemin nu est paginé.
    this.sharedService.url = API_ENDPOINTS.typedepotoirs.listPath;
    this.sharedService.getAll().subscribe({
      next: types => { this.listTypes = types ?? []; this.loadingTypes = false; },
      error: () => this.loadingTypes = false,
    });

    // `GET /v1/quartiers/s` met environ 5 secondes et pèse ~194 Ko : les 357 quartiers sont
    // renvoyés avec leur géométrie complète, dont ce menu n'a aucun usage. Sans indicateur, le
    // champ restait vide sans explication et paraissait cassé. Le vrai correctif serait un
    // read-model léger (id + nom) côté serveur — hors périmètre de cette passe.
    this.sharedService.url = API_ENDPOINTS.quartiers.listPath;
    this.sharedService.getAll().subscribe({
      next: quartiers => { this.listQuartiers = quartiers ?? []; this.loadingQuartiers = false; },
      error: () => this.loadingQuartiers = false,
    });
  }

  // convenience getter for easy access to form fields
  get f() { return this.depotoirForm.controls; }

  onSubmit() {
    this.submitted = true;
    if (this.depotoirForm.invalid) {
      return;
    }
    this.saving = true;

    const { typeDepotoirId, quartierId, address } = this.depotoirForm.value;
    const payload: any = {
      address,
      quartierId,
      typeDepotoir: { typeDepotoirId },
    };

    this.sharedService.url = API_ENDPOINTS.depotoirs.basePath;
    const request$ = this.isEdit
      ? this.sharedService.update(payload, this.id)
      : this.sharedService.create(payload);

    request$.pipe(first()).subscribe({
      next: () => {
        this.saving = false;
        succesAlert(this.isEdit ? 'Le point a bien été mis à jour' : 'Le point a bien été créé');
        this.createDepotoirModal.close(true);
      },
      error: (error) => {
        this.saving = false;
        errorAlert('Erreur : ' + (error?.message ?? error));
      },
    });
  }
}
