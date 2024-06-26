import { Component,  ElementRef, Injectable, Injector, Type, ViewChild, ViewContainerRef, ViewEncapsulation, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedService } from '../../../services/shared.service';
import { SuccessComponent } from '../../../shared/success/success.component';
import { first } from 'rxjs';
import { ComponentService } from '../../../services/component.service';
import { Geometry } from '../../../models/geometry.model';

@Component({
  selector: 'app-create-department',
  templateUrl: './create-department.component.html',
  styleUrl: './create-department.component.scss'
})
export class CreateDepartmentComponent {


  isSelected: any = "label_after";
  selected: boolean = false;
  listRegions: any;
  selectedItemsRegion: any[] = [];
  regionSettings = {};
  submitted = false;
  departForm!: FormGroup;
  id: any;
  currentDepart: any;
  receivedData: any

  successDialogRef!: MatDialogRef<SuccessComponent>;

  constructor(
    private createDepartModal: MatDialogRef<CreateDepartmentComponent>,
    private el: ElementRef,
    private matDialog: MatDialog,
    private formBuilder: FormBuilder,
    private router: Router,
    private sharedService: SharedService,
    private componentService: ComponentService,


  ) { }

  ngOnInit(): void {
    this.departForm = this.formBuilder.group({
      departmentName: ['', Validators.required],
      departmentCode: ['', Validators.required],
      region: [null, Validators.required],
      geometry:[null],
    });
    console.log(this.id);
    
    if (this.id != undefined){
      this.departForm.patchValue(this.currentDepart);
      this.departForm.controls['geometry'].setValue(null)
    }
    this.selectedItemsRegion = [];

    this.regionSettings = {
      singleSelection: true,
      idField: 'regionId',
      textField: 'regionName',
      itemsShowLimit: 3,
      allowSearchFilter: false,
      enableCheckAll: false,
    }

    this.componentService.getObservable().subscribe((data) => {
      this.receivedData = data;
      console.log("Data from file upload: ",data);
      
    });

  }

  message = "";
    receiveMessage($event: any) {
      this.message = $event;
      console.log(this.message );
      
    }

  get fileUpload() {
    return this.componentService.getInputComponents("Géométry");
  }


  closeCreateDepartModal() {
    this.createDepartModal.close(false)
    this.openSuccessModal()
  }
  
  closeSuccessModal() {
    this.successDialogRef.close();
  }

  openSuccessModal() {
    this.successDialogRef = this.matDialog.open(SuccessComponent, {
      disableClose: false,
      panelClass: ['success-with-dialog'],
    });
    let message = "Département créé avec succés";
    if(this.id != undefined)
      message = "Département modifié avec succés";

    this.successDialogRef.componentInstance.message = message;
  }
    // Event drop down to select role
    onDropDownCloseRegion() {
      let myTag = this.el.nativeElement.querySelector("label.label-role");
      if (this.selectedItemsRegion.length != 0) {
        console.log(this.selectedItemsRegion.length);
        myTag.classList.add('label_after');
      } else {
        myTag.classList.remove('label_after');
      }
      this.sharedService.url = "/regions/all"
      this.sharedService.getAll()
        .subscribe(regions => this.listRegions = regions);
      console.log("AfterView" + JSON.stringify(this.listRegions));
    }
  
  
    // select methods for role
    onItemSelectRegion(item: any) {
      this.selectedItemsRegion.push(item)
      //console.log('form model', this.selectedItemsRegion);
    }
  
    onItemDeSelectRegion(item: any) {
      this.selectedItemsRegion = this.selectedItemsRegion.filter(itm => itm.item_id !== item.item_id)
      //console.log('form model', this.selectedItemsRegion);
    }

  // convenience getter for easy access to form fields
  get f() { return this.departForm.controls; }

  onSubmit() {
    this.submitted = true;
    this.departForm.value.geometry = new Geometry(this.receivedData);
    this.departForm.value.region = this.departForm.value.region[0]
    this.sharedService.url = '/department';
    console.log(this.departForm.value)

    if (this.id != undefined)
      this.updateDepart();
    else
      this.createDepart();

  }


  createDepart() {

   this.sharedService.create(this.departForm.value)
      .pipe(first())
      .subscribe({
        next: () => {
          this.reload("/departemsnts")
          this.closeCreateDepartModal()
          setTimeout(() => {
            this.closeSuccessModal()
          }, 1500);

        },
        error: error => {
          console.log(error)
        }
      })
  }

  updateDepart() {
 
    //this.departForm.value.authority = this.departForm.value.authority[0];
    this.sharedService.update(this.departForm.value, this.id)
      .pipe(first())
      .subscribe({
        next: () => {
          this.reload("/departements")
          this.closeCreateDepartModal()
          setTimeout(() => {
            this.closeSuccessModal()
          }, 1500);

        },
        error: error => {
          console.log(error)
        }
      })
  }

  async reload(url: string): Promise<boolean> {
    await this.router.navigateByUrl('/', { skipLocationChange: true });
    return this.router.navigateByUrl(url);
  }
      
}



