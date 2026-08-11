import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { first } from 'rxjs';
import { SharedService } from '../../../services/shared.service';
import { API_ENDPOINTS } from '../../../shared/constants/api-endpoints';
import { succesAlert, errorAlert } from '../../../services/alert.service';

/**
 * Création / modification d'un circuit de collecte.
 *
 * <p>Le composant était un squelette vide (aucun formulaire, aucun appel) : le bouton
 * « Ajouter circuit » ouvrait un dialogue sans champs. Construit ici d'après le DTO
 * `CircuitCollect` réellement exposé par le backend.
 */
@Component({
    selector: 'app-create-circuit-collect',
    templateUrl: './create-circuit-collect.component.html',
    styleUrl: './create-circuit-collect.component.scss',
    standalone: false
})
export class CreateCircuitCollectComponent {

  submitted = false;
  saving = false;
  circuitForm!: FormGroup;
  id: any;
  currentCircuitCollect: any;
  listCommunes: any[] = [];

  constructor(
    private createCircuitModal: MatDialogRef<CreateCircuitCollectComponent>,
    private formBuilder: FormBuilder,
    private sharedService: SharedService,
  ) { }

  ngOnInit(): void {
    this.circuitForm = this.formBuilder.group({
      name: ['', Validators.required],
      code: ['', Validators.required],
      communeId: [null, Validators.required],
      type: [''],
      cat: [''],
      frequency: [''],
      rotation: [''],
      length: [''],
    });

    if (this.id != undefined) {
      this.circuitForm.patchValue(this.currentCircuitCollect);
    }

    this.sharedService.url = API_ENDPOINTS.communes.listPath;
    this.sharedService.getAll().subscribe(communes => this.listCommunes = communes);
  }

  get f() { return this.circuitForm.controls; }

  onSubmit() {
    this.submitted = true;
    if (this.circuitForm.invalid) {
      return;
    }
    this.saving = true;
    this.sharedService.url = API_ENDPOINTS['circuit-collects'].basePath;

    const request$ = this.id != undefined
      ? this.sharedService.update(this.circuitForm.value, this.id)
      : this.sharedService.create(this.circuitForm.value);

    request$.pipe(first()).subscribe({
      next: () => {
        this.saving = false;
        succesAlert(this.id != undefined ? 'Le circuit a bien été mis à jour' : 'Le circuit a bien été créé');
        this.createCircuitModal.close(true);
      },
      error: error => {
        this.saving = false;
        errorAlert('Erreur : ' + (error?.message ?? error));
      },
    });
  }
}
