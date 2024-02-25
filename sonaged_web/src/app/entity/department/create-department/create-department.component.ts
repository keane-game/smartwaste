import { Component, ElementRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedService } from '../../../services/shared.service';
import { SuccessComponent } from '../../../shares/success/success.component';

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


  successDialogRef!: MatDialogRef<SuccessComponent>;
  constructor(
    private createDepartModal: MatDialogRef<CreateDepartmentComponent>,
    private el: ElementRef,
    private matDialog: MatDialog,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private sharedService: SharedService,

  ) { }

  ngOnInit(): void {
    this.departForm = this.formBuilder.group({
      departmentName: ['', Validators.required],
      departmentCode: ['', Validators.required],
      region: ['', Validators.required],
      geometry:[''],
    });


    this.selectedItemsRegion = [];

    this.regionSettings = {
      singleSelection: true,
      idField: 'regionId',
      textField: 'regionName',
      itemsShowLimit: 3,
      allowSearchFilter: false,
      enableCheckAll: false,
    }

  }

    // Event drop down to select role
    onDropDownCloseRole() {
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
    onItemSelectRole(item: any) {
      this.selectedItemsRegion.push(item)
      //console.log('form model', this.selectedItemsRegion);
    }
  
    onItemDeSelectRole(item: any) {
      this.selectedItemsRegion = this.selectedItemsRegion.filter(itm => itm.item_id !== item.item_id)
      //console.log('form model', this.selectedItemsRegion);
    }

    onSubmit() {
      throw new Error('Method not implemented.');
      }
      
}



