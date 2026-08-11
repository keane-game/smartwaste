import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { SharedService } from '../../../services/shared.service';
import { first } from 'rxjs';
import { ComponentService } from '../../../services/component.service';
import { API_ENDPOINTS } from '../../../shared/constants/api-endpoints';
import { Geometry } from '../../../models/geometry.model';
import { succesAlert, errorAlert } from '../../../services/alert.service';

@Component({
    selector: 'app-create-department',
    templateUrl: './create-department.component.html',
    styleUrl: './create-department.component.scss',
    standalone: false
})
export class CreateDepartmentComponent {

  submitted = false;
  departForm!: FormGroup;
  id: any;
  currentDepart: any;
  receivedData: any;
  listRegions: any[] = [];

  constructor(
    private createDepartModal: MatDialogRef<CreateDepartmentComponent>,
    private formBuilder: FormBuilder,
    private sharedService: SharedService,
    private componentService: ComponentService,
  ) { }

  ngOnInit(): void {
    this.departForm = this.formBuilder.group({
      departmentName: ['', Validators.required],
      departmentCode: ['', Validators.required],
      regionId: [null, Validators.required],
      geometry: [null],
    });

    if (this.id != undefined) {
      this.departForm.patchValue(this.currentDepart);
      this.departForm.controls['geometry'].setValue(null);
    }

    // `listPath` (`/regions/s`), pas `basePath` : liste complète, pas la variante paginée.
    this.sharedService.url = API_ENDPOINTS.regions.listPath;
    this.sharedService.getAll().subscribe(regions => this.listRegions = regions);

    this.componentService.getObservable().subscribe((data) => {
      this.receivedData = data;
    });
  }

  get fileUpload() {
    return this.componentService.getInputComponents("Géométry");
  }

  // convenience getter for easy access to form fields
  get f() { return this.departForm.controls; }

  onSubmit() {
    this.submitted = true;
    if (this.departForm.invalid) {
      return;
    }
    if (this.receivedData) {
      this.departForm.value.geometry = new Geometry(this.receivedData);
    }
    this.sharedService.url = API_ENDPOINTS.departments.basePath;

    if (this.id != undefined) {
      this.updateDepart();
    } else {
      this.createDepart();
    }
  }

  createDepart() {
    this.sharedService.create(this.departForm.value)
      .pipe(first())
      .subscribe({
        next: () => {
          succesAlert('Le département a bien été créé');
          this.createDepartModal.close(true);
        },
        error: error => errorAlert('Erreur : ' + (error?.message ?? error)),
      });
  }

  updateDepart() {
    this.sharedService.update(this.departForm.value, this.id)
      .pipe(first())
      .subscribe({
        next: () => {
          succesAlert('Le département a bien été mis à jour');
          this.createDepartModal.close(true);
        },
        error: error => errorAlert('Erreur : ' + (error?.message ?? error)),
      });
  }
}
