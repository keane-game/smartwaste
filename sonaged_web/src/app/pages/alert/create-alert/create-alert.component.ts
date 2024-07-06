import { Component, ElementRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedService } from '../../../services/shared.service';
import { first } from 'rxjs';

@Component({
  selector: 'app-create-alert',
  templateUrl: './create-alert.component.html',
  styleUrl: './create-alert.component.scss'
})
export class CreateAlertComponent {

  
  isSelected: any = "label_after";
  selected: boolean = false;
  listDeparts: any;
  selectedItemsDepart: any[] = [];
  dpartSettings = {};
  submitted = false;
  alertForm!: FormGroup;
  currentAlert: any;
  id:any;
  selectedFile!: File ;

  constructor(
    private el: ElementRef,
    private matDialog: MatDialog,
    private formBuilder: FormBuilder,
    private router: Router,
    private sharedService: SharedService,

  ) { }

  ngOnInit(): void {
    this.alertForm = this.formBuilder.group({
      object: ['', Validators.required],
      code: ['', Validators.required],
      message: [null, Validators.required]
    });

    if (this.id != undefined){
      this.alertForm.patchValue(this.currentAlert);
      this.alertForm.controls['geometry'].setValue(null)
      this.alertForm.controls['geometry'].disabled
    }

  this.selectedItemsDepart = [];

  this.dpartSettings = {
    singleSelection: true,
    idField: 'departmentId',
    textField: 'departmentName',
    itemsShowLimit: 3,
    allowSearchFilter: false,
    enableCheckAll: false,
  }

}

 onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0];
  }
onSubmit() {

  this.submitted = true;
  this.sharedService.url = '/alerts';
  console.log(this.alertForm.value)

  if (this.id != undefined)
    this.createAlert();
  else
    this.createAlert();

}




createAlert() {

  const formData: FormData = new FormData();

  formData.append('file', this.selectedFile);
  formData.append('alert',JSON.stringify( this.alertForm.value));
  console.log(formData)

  this.sharedService.create(formData)
     .pipe(first())
     .subscribe({
       next: () => {
        console.log("formData")
       },
       error: error => {
         console.log(error)
       }
     })
 }


}
