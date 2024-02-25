import { Component, ElementRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedService } from '../../../services/shared.service';
import { SuccessComponent } from '../../../shares/success/success.component';

@Component({
  selector: 'app-create-commune',
  templateUrl: './create-commune.component.html',
  styleUrl: './create-commune.component.scss'
})
export class CreateCommuneComponent {

  isSelected: any = "label_after";
  selected: boolean = false;
  listDeparts: any;
  selectedItemsCommune: any[] = [];
  communeSettings = {};
  submitted = false;
  communeForm!: FormGroup;

  successDialogRef!: MatDialogRef<SuccessComponent>;
  constructor(
    private createDepartModal: MatDialogRef<CreateCommuneComponent>,
    private el: ElementRef,
    private matDialog: MatDialog,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private sharedService: SharedService,

  ) { }

  ngOnInit(): void {
      this.communeForm = this.formBuilder.group({
        communeName: ['', Validators.required],
        communeCode: ['', Validators.required],
        department: ['', Validators.required],
        totalResident:[''],
        womanResident:[''],
        manResident:[''],
        communeLength:[''],
        communeArea: [''],
        geometry: ['']
      });

    this.selectedItemsCommune = [];

    this.communeSettings = {
      singleSelection: true,
      idField: 'regionId',
      textField: 'regionName',
      itemsShowLimit: 3,
      allowSearchFilter: false,
      enableCheckAll: false,
    }

  }

  // Event drop down to select Commune
  onDropDownCloseCommune() {
    let myTag = this.el.nativeElement.querySelector("label.label-Commune");
    if (this.selectedItemsCommune.length != 0) {
      console.log(this.selectedItemsCommune.length);
      myTag.classList.add('label_after');
    } else {
      myTag.classList.remove('label_after');
    }
    this.sharedService.url = "/department/all"
    this.sharedService.getAll()
      .subscribe(departments => this.listDeparts = departments);
    console.log("AfterView" + JSON.stringify(this.listDeparts));
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

  onSubmit() {
    throw new Error('Method not implemented.');
    }
}
