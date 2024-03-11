import { Component, ElementRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedService } from '../../../services/shared.service';
import { SuccessComponent } from '../../../shares/success/success.component';
import { CreateDepotoirComponent } from '../../depotoir/create/create-depotoir.component';

@Component({
  selector: 'app-create-circuit',
  templateUrl: './create-circuit.component.html',
  styleUrl: './create-circuit.component.scss'
})
export class CreateCircuitComponent {

  isSelected: any = "label_after";
  selected: boolean = false;
  listDepotoirs: any;
  listQuartiers: any;
  selectedItemsDepotoir: any[] = [];
  depotoirSettings = {};
  selectedItemsQuartier: any[] = [];
  quartierSettings = {};
  submitted = false;
  circuitForm!: FormGroup;
  depotoirForm: any;

  successDialogRef!: MatDialogRef<SuccessComponent>;
  constructor(
    private createDepotoirModal: MatDialogRef<CreateDepotoirComponent>,
    private el: ElementRef,
    private matDialog: MatDialog,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private sharedService: SharedService,

  ) { }

  ngOnInit(): void {
    this.depotoirForm = this.formBuilder.group({
      depotoirAddress: ['', Validators.required],
      typeDepotoir: ['', Validators.required],
      quartier: ['', Validators.required],
      longitude:[''],
      latitude: ['']
    });


    this.selectedItemsDepotoir = [];

    this.depotoirSettings = {
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


    // Event drop down to select role
    onDropDownCloseRole() {
      let myTag = this.el.nativeElement.querySelector("label.label-role");
      if (this.selectedItemsDepotoir.length != 0) {
        console.log(this.selectedItemsDepotoir.length);
        myTag.classList.add('label_after');
      } else {
        myTag.classList.remove('label_after');
      }
      this.sharedService.url = "/depotoirs/all"
      this.sharedService.getAll().subscribe(depotoirs => this.listDepotoirs = depotoirs);
      console.log("AfterView" + JSON.stringify(this.listDepotoirs));
    }
  
  
    // select methods for role
    onItemSelectRole(item: any) {
      this.selectedItemsDepotoir.push(item)
      //console.log('form model', this.selectedItemsDepotoir);
    }
  
    onItemDeSelectRole(item: any) {
      this.selectedItemsDepotoir = this.selectedItemsDepotoir.filter(itm => itm.item_id !== item.item_id)
      //console.log('form model', this.selectedItemsDepotoir);
    }

    onSubmit() {
      throw new Error('Method not implemented.');
      }
}
