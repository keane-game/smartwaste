import { Component, ElementRef } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { MatDialogRef, MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedService } from '../../../services/shared.service';
import { SuccessComponent } from '../../../shared/success/success.component';
import { first } from 'rxjs';

@Component({
  selector: 'app-create-depotoir',
  templateUrl: './create-depotoir.component.html',
  styleUrls: ['./create-depotoir.component.scss']
})
export class CreateDepotoirComponent {


  isSelected: any = "label_after";
  selected: boolean = false;
  listTDepotoirs: any;
  listQuartiers: any;
  selectedItemsDepotoir: any[] = [];
  tdepotoirSettings = {};
  selectedItemsQuartier: any[] = [];
  quartierSettings = {};
  submitted = false;
  depotoirForm!: FormGroup;
  currentDepotoir: any;
  id: any;

  successDialogRef!: MatDialogRef<SuccessComponent>;
  constructor(
    private createDepotoirModal: MatDialogRef<CreateDepotoirComponent>,
    private el: ElementRef,
    private matDialog: MatDialog,
    private formBuilder: FormBuilder,
    private router: Router,
    private sharedService: SharedService,

  ) { }

  ngOnInit(): void {
    this.depotoirForm = this.formBuilder.group({
      depotoirAddress: ['', Validators.required],
      typeDepotoir: ['', Validators.required],
      quartier: ['', Validators.required],
      longitude:[''],
      latitude: [''],
      geometry: [null]
    });
    if (this.id != undefined){
      this.depotoirForm.patchValue(this.currentDepotoir);
      this.depotoirForm.controls['geometry'].setValue(null)
      this.depotoirForm.controls['geometry'].disabled
    }

    this.selectedItemsDepotoir = [];

    this.tdepotoirSettings = {
      singleSelection: true,
      idField: 'typeDepotoirId',
      textField: 'typeDepotoirName',
      itemsShowLimit: 3,
      allowSearchFilter: false,
      enableCheckAll: false,
    }


    this.selectedItemsQuartier = [];

    this.quartierSettings = {
      singleSelection: true,
      idField: 'quartierId',
      textField: 'quartierName',
      itemsShowLimit: 3,
      allowSearchFilter: false,
      enableCheckAll: false,
    }
    

  }

  closeCreateDepartModal() {
    this.createDepotoirModal.close(false)
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
    let message = "Dépotoir créée avec succés";
    if(this.id != undefined)
      message = "Dépotoir modifiée avec succés";

    this.successDialogRef.componentInstance.message = message;
  }

    // Event drop down to select role
    onDropDownCloseQuartier() {
      let myTag = this.el.nativeElement.querySelector("label.label-quartier");
      if (this.selectedItemsQuartier.length != 0) {
        console.log(this.selectedItemsQuartier.length);
        myTag.classList.add('label_after');
      } else {
        myTag.classList.remove('label_after');
      }
      this.sharedService.url = "/quartier"
      this.sharedService.getAll()
        .subscribe(quartier => this.listQuartiers = quartier);
      console.log("AfterView" + JSON.stringify(this.listQuartiers));
    }
  
  
    // select methods for role
    onItemSelectQuartier(item: any) {
      this.selectedItemsDepotoir.push(item)
      //console.log('form model', this.selectedItemsDepotoir);
    }
  
    onItemDeSelectQuartier(item: any) {
      this.selectedItemsDepotoir = this.selectedItemsDepotoir.filter(itm => itm.item_id !== item.item_id)
      //console.log('form model', this.selectedItemsDepotoir);
    }


    // Event drop down to select role
    onDropDownCloseTDepotoir() {
      let myTag = this.el.nativeElement.querySelector("label.label-tdepotoir");
      if (this.selectedItemsQuartier.length != 0) {
        console.log(this.selectedItemsQuartier.length);
        myTag.classList.add('label_after');
      } else {
        myTag.classList.remove('label_after');
      }
      this.sharedService.url = "/typedepotoir"
      this.sharedService.getAll()
        .subscribe(tdepotoirs => this.listTDepotoirs = tdepotoirs);
      console.log("AfterView" + JSON.stringify(this.listTDepotoirs));
    }
  
  
    // select methods for role
    onItemSelectTDepotoir(item: any) {
      this.selectedItemsDepotoir.push(item)
      //console.log('form model', this.selectedItemsDepotoir);
    }
  
    onItemDeSelectTDepotoir(item: any) {
      this.selectedItemsDepotoir = this.selectedItemsDepotoir.filter(itm => itm.item_id !== item.item_id)
      //console.log('form model', this.selectedItemsDepotoir);
    }

  
 // convenience getter for easy access to form fields
 get f() { return this.depotoirForm.controls; }

 onSubmit() {
   this.submitted = true;
   this.sharedService.url = '/depotoir';
   console.log(this.depotoirForm.value)

   if (this.id != undefined)
     this.updateDepotoir();
   else
     this.createDepotoir();

 }

 createDepotoir() {

  this.sharedService.create(this.depotoirForm.value)
     .pipe(first())
     .subscribe({
       next: () => {
         this.reload("/depotoirs")
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

 updateDepotoir() {

   //this.depotoirForm.value.authority = this.depotoirForm.value.authority[0];
   this.sharedService.update(this.depotoirForm.value, this.id)
     .pipe(first())
     .subscribe({
       next: () => {
         this.reload("/depotoirs")
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
