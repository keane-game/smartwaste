import { Component, Inject } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { SharedService } from '../../../services/shared.service';
import { first } from 'rxjs';

import { succesAlert, errorAlert } from '../../../services/alert.service';
import { API_ENDPOINTS } from '../../../shared/constants/api-endpoints';

@Component({
    selector: 'app-create-quartier',
    templateUrl: './create-quartier.component.html',
    styleUrl: './create-quartier.component.scss',
    standalone: false
})
export class CreateQuartierComponent {

  submitted = false;
  quartierForm!: FormGroup;
  currentQuartier: any;
  id: any;
  listCommunes: any[] = [];

  constructor(
    private createQuartierModal: MatDialogRef<CreateQuartierComponent>,
    private formBuilder: FormBuilder,
    private sharedService: SharedService,
    @Inject(MAT_DIALOG_DATA) public data: any,
  ) {
    this.id = data.id;
    this.currentQuartier = data.currentQuartier;
  }

  ngOnInit(): void {
    // Noms de controle alignes sur le DTO `Quartier` cote backend (P1-2). Le formulaire
    // precedent avait `commune` (le DTO attend `communeId`) et le template referencait
    // `formControlName="quartierCav"` — un controle qui n'existait meme pas dans ce FormGroup
    // (seul `cav` y figurait) : Angular levait "Cannot find control with name: 'quartierCav'"
    // des l'ouverture du dialogue.
    this.quartierForm = this.formBuilder.group({
      name: ['', Validators.required],
      code: ['', Validators.required],
      communeId: [null, Validators.required],
      cav: [''],
      codeCav: [''],
      cCrca: [''],
      codeCcrca: [''],
      codeEntity: [''],
      numerozr: [''],
      codeSzr: [''],
      zoneCoron: [''],
      poucentage: [''],
      length: [''],
      area: [''],
      geometry: [null],
    });

    if (this.id != undefined) {
      this.quartierForm.patchValue(this.currentQuartier);
      this.quartierForm.controls['geometry'].setValue(null);
    }

    this.sharedService.url = API_ENDPOINTS.communes.listPath;
    this.sharedService.getAll().subscribe(communes => this.listCommunes = communes);
  }

  // convenience getter for easy access to form fields
  get f() { return this.quartierForm.controls; }

  onSubmit() {
    this.submitted = true;
    if (this.quartierForm.invalid) {
      return;
    }
    this.sharedService.url = API_ENDPOINTS.quartiers.basePath;

    if (this.id != undefined) {
      this.updateQuartier();
    } else {
      this.createQuartier();
    }
  }

  createQuartier() {
    this.sharedService.create(this.quartierForm.value)
      .pipe(first())
      .subscribe({
        next: () => {
          succesAlert('Le quartier a bien été créé');
          this.createQuartierModal.close(true);
        },
        error: error => errorAlert('Erreur : ' + (error?.message ?? error)),
      });
  }

  updateQuartier() {
    this.sharedService.update(this.quartierForm.value, this.id)
      .pipe(first())
      .subscribe({
        next: () => {
          succesAlert('Le quartier a bien été mis à jour');
          this.createQuartierModal.close(true);
        },
        error: error => errorAlert('Erreur : ' + (error?.message ?? error)),
      });
  }
}
