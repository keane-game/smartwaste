import { Component, ElementRef } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { MatDialogRef, MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedService } from '../../../services/shared.service';
import { SuccessComponent } from '../../../shares/success/success.component';
import { first } from 'rxjs';

@Component({
  selector: 'app-create-quartier',
  templateUrl: './create-quartier.component.html',
  styleUrl: './create-quartier.component.scss'
})
export class CreateQuartierComponent {


  isSelected: any = "label_after";
  selected: boolean = false;
  listCommunes: any;
  selectedItemsCommune: any[] = [];
  communeSettings = {};
  submitted = false;
  quartierForm!: FormGroup;
  currentQuartier: any;
  id: any;

  successDialogRef!: MatDialogRef<SuccessComponent>;
  constructor(
    private createCommuneModal: MatDialogRef<CreateQuartierComponent>,
    private el: ElementRef,
    private matDialog: MatDialog,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private sharedService: SharedService,

  ) { }

  ngOnInit(): void {
    this.quartierForm = this.formBuilder.group({
      quartierName: ['', Validators.required],
      quartierCode: ['', Validators.required],
      quartierCav: ['', Validators.required],
      quartierCodeCav:[''],
      quartierCcrca: [''],
      quartierCodeEntity: [''],
      quartierNumerozr: [''],
      quartierZoneCoron: [''],
      quartierPoucentage: [''],
      quartierLength: [''],
      quartierArea: [''],
      commune: [null],
      geometry: [null]

    });

    if (this.id != undefined){
      this.quartierForm.patchValue(this.currentQuartier);
      this.quartierForm.controls['geometry'].setValue(null)
      this.quartierForm.controls['geometry'].disabled
    }

    this.selectedItemsCommune = [];

    this.communeSettings = {
      singleSelection: true,
      idField: 'communeId',
      textField: 'communeName',
      itemsShowLimit: 3,
      allowSearchFilter: false,
      enableCheckAll: false,
    }

  }


  closeCreateQuartierModal() {
    this.createCommuneModal.close(false)
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
    let message = "Quartier créée avec succés";
    if(this.id != undefined)
      message = "Quartier modifiée avec succés";

    this.successDialogRef.componentInstance.message = message;
  }

   // Event drop down to select Commune
   onDropDownCloseCommune() {
    let myTag = this.el.nativeElement.querySelector("label.label-commune");
    if (this.selectedItemsCommune.length != 0) {
      console.log(this.selectedItemsCommune.length);
      myTag.classList.add('label_after');
    } else {
      myTag.classList.remove('label_after');
    }
    this.sharedService.url = "/commune"
    this.sharedService.getAll()
      .subscribe(communes => this.listCommunes = communes);
    console.log("AfterView" + JSON.stringify(this.listCommunes));
  }


  // select methods for Commune
  onItemSelectCommune(item: any) {
    this.selectedItemsCommune.push(item)
    //console.log('form model', this.selectedItemsCommune);
  }

  onItemDeSelectCommune(item: any) {
    this.selectedItemsCommune = this.selectedItemsCommune.filter(itm => itm.item_id !== item.item_id)
    //console.log('form model', this.selectedItemsCommune);
  }


   // convenience getter for easy access to form fields
   get f() { return this.quartierForm.controls; }

   onSubmit() {
     this.submitted = true;
     this.sharedService.url = '/quartier';
     console.log(this.quartierForm.value)
 
     if (this.id != undefined)
       this.updateCommune();
     else
       this.createCommune();
 
   }


  createCommune() {

    this.sharedService.create(this.quartierForm.value)
       .pipe(first())
       .subscribe({
         next: () => {
           this.reload("/quatiers")
           this.closeCreateQuartierModal()
           setTimeout(() => {
             this.closeSuccessModal()
           }, 1500);
 
         },
         error: error => {
           console.log(error)
         }
       })
   }
 
   updateCommune() {
  
     //this.quartierForm.value.authority = this.quartierForm.value.authority[0];
     this.sharedService.update(this.quartierForm.value, this.id)
       .pipe(first())
       .subscribe({
         next: () => {
           this.reload("/quatiers")
           this.closeCreateQuartierModal()
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
