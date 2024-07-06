import { Component, ElementRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedService } from '../../../services/shared.service';
import { first } from 'rxjs';

@Component({
  selector: 'app-create-commune',
  templateUrl: './create-commune.component.html',
  styleUrl: './create-commune.component.scss'
})
export class CreateCommuneComponent {

  isSelected: any = "label_after";
  selected: boolean = false;
  listDeparts: any;
  selectedItemsDepart: any[] = [];
  dpartSettings = {};
  submitted = false;
  communeForm!: FormGroup;
  currentCommune: any;
  id:any;

  constructor(
    private createDepartModal: MatDialogRef<CreateCommuneComponent>,
    private el: ElementRef,
    private matDialog: MatDialog,
    private formBuilder: FormBuilder,
    private router: Router,
    private sharedService: SharedService,

  ) { }

  ngOnInit(): void {
      this.communeForm = this.formBuilder.group({
        name: ['', Validators.required],
        code: ['', Validators.required],
        department: [null, Validators.required],
        total:[''],
        women:[''],
        man:[''],
        length:[''],
        area: [''],
        geometry: [null]
      });

      if (this.id != undefined){
        this.communeForm.patchValue(this.currentCommune);
        this.communeForm.controls['geometry'].setValue(null)
        this.communeForm.controls['geometry'].disabled
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


  // Event drop down to select Commune
  onDropDownCloseDepart() {
    let myTag = this.el.nativeElement.querySelector("label.label-Depart");
    if (this.selectedItemsDepart.length != 0) {
      console.log(this.selectedItemsDepart.length);
      myTag.classList.add('label_after');
    } else {
      myTag.classList.remove('label_after');
    }
    this.sharedService.url = "/department"
    this.sharedService.getAll()
      .subscribe(departments => this.listDeparts = departments);
    console.log("AfterView" + JSON.stringify(this.listDeparts));
  }


  // select methods for Depart
  onItemSelectDepart(item: any) {
    this.selectedItemsDepart.push(item)
    //console.log('form model', this.selectedItemsDepart);
  }

  onItemDeSelectDepart(item: any) {
    this.selectedItemsDepart = this.selectedItemsDepart.filter(itm => itm.item_id !== item.item_id)
    //console.log('form model', this.selectedItemsCommune);
  }


 // convenience getter for easy access to form fields
  get f() { return this.communeForm.controls; }

  onSubmit() {
    this.submitted = true;
    this.sharedService.url = '/commune';
    console.log(this.communeForm.value)

    if (this.id != undefined)
      this.updateCommune();
    else
      this.createCommune();

  }


  createCommune() {

    this.sharedService.create(this.communeForm.value)
       .pipe(first())
       .subscribe({
         next: () => {
          
         },
         error: error => {
           console.log(error)
         }
       })
   }
 
   updateCommune() {
  
     //this.communeForm.value.authority = this.communeForm.value.authority[0];
     this.sharedService.update(this.communeForm.value, this.id)
       .pipe(first())
       .subscribe({
         next: () => {
          
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
