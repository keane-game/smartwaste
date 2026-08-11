import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { first } from 'rxjs';
import { SharedService } from '../../../services/shared.service';
import { API_ENDPOINTS } from '../../../shared/constants/api-endpoints';
import { succesAlert, errorAlert } from '../../../services/alert.service';

@Component({
    selector: 'app-create-region',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, MatDialogModule],
    templateUrl: './create-region.component.html',
    styleUrl: './create-region.component.scss'
})
export class CreateRegionComponent {

  submitted = false;
  regionForm!: FormGroup;
  id: any;
  currentRegion: any;

  constructor(
    private createRegionModal: MatDialogRef<CreateRegionComponent>,
    private formBuilder: FormBuilder,
    private sharedService: SharedService,
  ) { }

  ngOnInit(): void {
    this.regionForm = this.formBuilder.group({
      name: ['', Validators.required],
      code: ['', Validators.required],
    });

    if (this.id != undefined) {
      this.regionForm.patchValue(this.currentRegion);
    }
  }

  get f() { return this.regionForm.controls; }

  onSubmit() {
    this.submitted = true;
    if (this.regionForm.invalid) {
      return;
    }
    this.sharedService.url = API_ENDPOINTS.regions.basePath;

    if (this.id != undefined) {
      this.updateRegion();
    } else {
      this.createRegion();
    }
  }

  createRegion() {
    this.sharedService.create(this.regionForm.value)
      .pipe(first())
      .subscribe({
        next: () => {
          succesAlert('La région a bien été créée');
          this.createRegionModal.close(true);
        },
        error: error => errorAlert('Erreur : ' + (error?.message ?? error)),
      });
  }

  updateRegion() {
    this.sharedService.update(this.regionForm.value, this.id)
      .pipe(first())
      .subscribe({
        next: () => {
          succesAlert('La région a bien été mise à jour');
          this.createRegionModal.close(true);
        },
        error: error => errorAlert('Erreur : ' + (error?.message ?? error)),
      });
  }
}
