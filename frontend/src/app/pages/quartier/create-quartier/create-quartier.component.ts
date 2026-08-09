import { Component, ElementRef, Inject } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedService } from '../../../services/shared.service';
import { first } from 'rxjs';

import { succesAlert, errorAlert } from '../../../services/alert.service';
import { ModalService } from '../../../services/modal.service';
import { API_ENDPOINTS } from '../../../shared/constants/api-endpoints';

@Component({
    selector: 'app-create-quartier',
    templateUrl: './create-quartier.component.html',
    styleUrl: './create-quartier.component.scss',
    standalone: false
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

  constructor(
    private createCommuneModal: MatDialogRef<CreateQuartierComponent>,
    private el: ElementRef,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private sharedService: SharedService,
    @Inject(MAT_DIALOG_DATA) public data:any,
  ) { 
    this.id = data.id
    this.currentQuartier = data.currentQuartier
    console.log(data)
  }

  ngOnInit(): void {
    this.quartierForm = this.formBuilder.group({
      name: ['', Validators.required],
      code: ['', Validators.required],
      cav: ['', Validators.required],
      codeCav: [''],
      ccrca: [''],
      codeEntity: [''],
      numerozr: [''],
      zoneCoron: [''],
      poucentage: [''],
      length: [''],
      area: [''],
      commune: [null],
      geometry: [null]

    });

    console.log(this.id);
    if (this.id != undefined) {
      this.quartierForm.patchValue(this.currentQuartier);
      this.quartierForm.controls['geometry'].setValue(null)
      this.quartierForm.controls['geometry'].disabled
    }

    this.selectedItemsCommune = [];

    this.communeSettings = {
      singleSelection: true,
      idField: 'communeId',
      textField: 'name',
      itemsShowLimit: 3,
      allowSearchFilter: false,
      enableCheckAll: false,
    }

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
    this.sharedService.url = API_ENDPOINTS.communes.listPath;
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
  url = '/regions';
  settings = {
    itemsShowLimit: 5,
    allowSearchFilter: true
  };

  onRegionSelected(region: any) {
    console.log('Selected region:', region);
  }

  onRegionDeselected(region: any) {
    console.log('Deselected region:', region);
  }

  // convenience getter for easy access to form fields
  get f() { return this.quartierForm.controls; }

  onSubmit() {
    console.log(this.id);
    this.submitted = true;
    this.sharedService.url = API_ENDPOINTS.quartiers.basePath;
    console.log(this.quartierForm.value)

    if (this.id != undefined)
      this.updateQuartier();
    else
      this.createQuartier();

  }


  createQuartier() {

    this.sharedService.create(this.quartierForm.value)
      .pipe(first())
      .subscribe({
        next:  () => {
          succesAlert("La création a bien réussi")
        },
        error: (error) => {
          errorAlert('Erreur' + error.message)
        }
      })
  }

  updateQuartier() {

    //this.quartierForm.value.authority = this.quartierForm.value.authority[0];
    this.sharedService.update(this.quartierForm.value, this.id)
      .pipe(first())
      .subscribe({
        next: () => {
          succesAlert("La suppression a bien réussi")
          this.reload("/quatiers")
        },
        error: error => {
          errorAlert('Erreur' + error.message)
        }
      })
  }

  async reload(url: string): Promise<boolean> {
    await this.router.navigateByUrl('/', { skipLocationChange: true });
    return this.router.navigateByUrl(url);
  }
}
