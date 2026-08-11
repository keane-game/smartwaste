import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { SharedService } from '../../../services/shared.service';
import { first } from 'rxjs';
import { API_ENDPOINTS } from '../../../shared/constants/api-endpoints';
import { succesAlert, errorAlert } from '../../../services/alert.service';

@Component({
    selector: 'app-create-commune',
    templateUrl: './create-commune.component.html',
    styleUrl: './create-commune.component.scss',
    standalone: false
})
export class CreateCommuneComponent {

  submitted = false;
  communeForm!: FormGroup;
  currentCommune: any;
  id: any;
  listDeparts: any[] = [];

  constructor(
    private createCommuneModal: MatDialogRef<CreateCommuneComponent>,
    private formBuilder: FormBuilder,
    private sharedService: SharedService,
  ) { }

  ngOnInit(): void {
    // Les noms de controle doivent correspondre exactement aux champs du DTO `Commune` cote
    // backend (name/code/total/women/men/length/area/departmentId) : le formulaire precedent
    // utilisait des noms totalement differents en HTML (`communeName`, `womanResident`...) qui ne
    // correspondaient meme pas aux noms declares ici en TS (`name`, `women`...) — Angular levait
    // une erreur des l'ouverture du dialogue ("Cannot find control with name"), et meme corrige
    // cote HTML seul, le JSON envoye au backend n'aurait jamais rempli les bons champs.
    this.communeForm = this.formBuilder.group({
      name: ['', Validators.required],
      code: ['', Validators.required],
      departmentId: [null, Validators.required],
      total: [''],
      women: [''],
      men: [''],
      length: [''],
      area: [''],
      geometry: [null],
    });

    if (this.id != undefined) {
      this.communeForm.patchValue(this.currentCommune);
      this.communeForm.controls['geometry'].setValue(null);
    }

    // `listPath` (`/departments/s`), pas `basePath` : c'est la liste complète (non paginée) —
    // le chemin nu exige désormais page&size (P1-2) et 500/400 sans eux.
    this.sharedService.url = API_ENDPOINTS.departments.listPath;
    this.sharedService.getAll().subscribe(departments => this.listDeparts = departments);
  }

  // convenience getter for easy access to form fields
  get f() { return this.communeForm.controls; }

  onSubmit() {
    this.submitted = true;
    if (this.communeForm.invalid) {
      return;
    }
    this.sharedService.url = API_ENDPOINTS.communes.basePath;

    if (this.id != undefined) {
      this.updateCommune();
    } else {
      this.createCommune();
    }
  }

  createCommune() {
    this.sharedService.create(this.communeForm.value)
      .pipe(first())
      .subscribe({
        next: () => {
          succesAlert('La commune a bien été créée');
          this.createCommuneModal.close(true);
        },
        error: error => errorAlert('Erreur : ' + (error?.message ?? error)),
      });
  }

  updateCommune() {
    this.sharedService.update(this.communeForm.value, this.id)
      .pipe(first())
      .subscribe({
        next: () => {
          succesAlert('La commune a bien été mise à jour');
          this.createCommuneModal.close(true);
        },
        error: error => errorAlert('Erreur : ' + (error?.message ?? error)),
      });
  }
}
