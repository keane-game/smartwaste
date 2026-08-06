import { Component, ElementRef, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MatDialog, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedService } from '../../../services/shared.service';
import { first } from 'rxjs';
import { succesAlert, errorAlert } from '../../../services/alert.service';
import * as L from 'leaflet';

@Component({
  selector: 'app-create-alert',
  templateUrl: './create-alert.component.html',
  styleUrl: './create-alert.component.scss'
})
export class CreateAlertComponent {
[x: string]: any;

  
  isSelected: any = "label_after";
  selected: boolean = false;
  listDeparts: any;
  selectedItemsDepart: any[] = [];
  dpartSettings = {};
  submitted = false;
  alertForm!: FormGroup;
  currentAlert: any;
  id:any;
  codes = ['WARNING', 'DANGER', 'INFO']
  selectedFile!: File ;

  constructor(
    private el: ElementRef,
    private matDialog: MatDialog,
    private formBuilder: FormBuilder,
    private router: Router,
    private sharedService: SharedService,

    @Inject(MAT_DIALOG_DATA) public data:any,
  ) { 
    this.id = data.id
    this.currentAlert = data.currentAlert
    console.log(data)
  }

  ngOnInit(): void {
    this.alertForm = this.formBuilder.group({
      object: ['', Validators.required],
      code: ['', Validators.required],
      message: [null, Validators.required],
      address: [null, Validators.required],
      coordinate: this.formBuilder.group({
        longitude: [''],
        latitude: [''],
        altitude: [''],
      })
    });

   
    if (this.id != undefined){
      this.selectUpdate()
      console.log(this.currentAlert)
      this.alertForm.patchValue(this.currentAlert);
    }
  }
  
  selectChange(event: Event) {
    const selectElement = event.target as HTMLSelectElement;
    console.log('Selected option:', selectElement.value);
    const myTag = this.el.nativeElement.querySelector("label.label-select");
    if ( selectElement.value) {
      console.log(selectElement.value);
      myTag.classList.remove('label_after');
    } else {
      myTag.classList.add('label_after');
      console.log(selectElement);
    }
  }
  
  selectUpdate() {
    const myTag = this.el.nativeElement.querySelector("label.label-select");
 
      myTag.classList.remove('label_after');
      console.log(myTag);
    
  }


  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0];
    console.log(this.selectedFile.name);
  }

  async onSubmit() {
       // Wait for the location to be fetched
    const locationResult = await this.getCurrentLocation();
    
    if (!locationResult.success) {
      errorAlert('Erreur: ' + locationResult.error);
      return;
    }
    
    if(this.alertForm.invalid){
      return
    }

    console.log(this.alertForm.value)
    this.submitted = true;
    this.sharedService.url = '/alerts';

    if (this.id != undefined)
      this.updateAlert();
    else
     this.createAlert();
  }

  createAlert() {
    const formData: FormData = new FormData();
  
    formData.append('file', this.selectedFile);
    formData.append('alert', JSON.stringify(this.alertForm.value));
    if(this.alertForm.value.coordinate.latitude == ""){
      this.alertForm.value.coordinate = null
    }
    console.log("formdata",formData);
    this.sharedService.create( formData)
    .pipe(first())
    .subscribe({
      next: () => {
        succesAlert("La création a bien réussie");
      },
      error: (error) => {
        errorAlert('Erreur ' + error.message);
      }
    });
  }
  
  updateAlert() {
    const formData: FormData = new FormData();
  
    formData.append('file', this.selectedFile);
    formData.append('alert', JSON.stringify(this.alertForm.value));
    console.log(formData);
    if(this.alertForm.value.coordinate.latitude == ""){
      this.alertForm.value.coordinate = null
    }
    this.sharedService.update( formData,this.id)
      .pipe(first())
      .subscribe({
        next: () => {
          succesAlert("La mise à jour a bien réussie");
        },
        error: (error) => {
          errorAlert('Erreur ' + error.message);
        }
      });
  }
 


 get f() { return this.alertForm.controls; }

 private async getCurrentLocation(): Promise<{ success: boolean, error?: string }> {
  return new Promise((resolve) => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        position => {
          const coords = position.coords;
          console.log(position);

          // Assign the coordinates to the alertForm
          this.alertForm.value.coordinate.latitude = coords.latitude;
          this.alertForm.value.coordinate.longitude = coords.longitude;
          this.alertForm.value.coordinate.altitude = coords.altitude;

          resolve({ success: true }); // Resolve with success
        },
        error => {
          console.error(error);
          resolve({ success: false, error: error.message }); // Resolve with error
        }
      );
    } else {
      const errorMessage = 'Geolocation is not supported by this browser.';
      console.error(errorMessage);
      resolve({ success: false, error: errorMessage }); // Resolve with error
    }
  });
}


private async getCurrentLocations(): Promise<void> {
  return new Promise((resolve, reject) => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        position => {
          const coords = position.coords;
          console.log(position);

          // Assign the coordinates to the alertForm
          this.alertForm.value.coordinate.latitude = coords.latitude;
          this.alertForm.value.coordinate.longitude = coords.longitude;
          this.alertForm.value.coordinate.altitude = coords.altitude;

          resolve(); // Resolve the promise once the coordinates are set
        },
        error => {
          console.error(error);
          reject(error); // Reject the promise if there's an error
        }
      );
    } else {
      const errorMessage = 'Geolocation is not supported by this browser.';
      console.error(errorMessage);
      reject(new Error(errorMessage)); // Reject the promise if geolocation is not supported
    }
  });
}

}