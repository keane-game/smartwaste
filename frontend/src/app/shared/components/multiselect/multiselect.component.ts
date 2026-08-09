import { CommonModule } from '@angular/common';
import { Component, ElementRef, Input, OnInit } from '@angular/core';
import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown';
import { SharedService } from '../../../services/shared.service';

@Component({
    selector: 'app-multiselect',
    imports: [
        CommonModule,
        NgMultiSelectDropDownModule
    ],
    templateUrl: './multiselect.component.html',
    styleUrls: ['./multiselect.component.scss']
})
export class MultiselectComponent implements OnInit {
  @Input() url!: string;
  @Input() settings: any;
  @Input() labelClass!: string;
  @Input() idField!: string;
  @Input() textField!: string;
  @Input() singleSelection: boolean = false;
  @Input() name: any

  @Input() listItems: any[] = [];
  selectedItems: any[] = [];
  
  dropdownSettings = {};

  constructor(
    private el: ElementRef,
    private sharedService: SharedService
  ) { }

  ngOnInit(): void {
    this.selectedItems = [];
    this.loadItems();

    this.dropdownSettings = {
      singleSelection: this.singleSelection,
      idField: this.idField,
      textField: this.textField,
      itemsShowLimit: 3,
      allowSearchFilter: true,
      enableCheckAll: !this.singleSelection,
      ...this.settings
    };
  }

  loadItems() {
    this.sharedService.url = this.url;
    this.sharedService.getAll().subscribe((items: any) => {
      this.listItems = items;
    });
  }

  onDropDownClose() {
    const myTag = this.el.nativeElement.querySelector(`label.${this.labelClass}`);
    if (this.selectedItems.length !== 0) {
      myTag.classList.add('label_after');
    } else {
      myTag.classList.remove('label_after');
    }
  }

  onItemSelect(item: any) {
    this.selectedItems.push(item);
  }

  onItemDeSelect(item: any) {
    this.selectedItems = this.selectedItems.filter(itm => itm[this.idField] !== item[this.idField]);
  }
}


// import { CommonModule } from '@angular/common';
// import { Component, ElementRef, Input } from '@angular/core';
// import { Router } from '@angular/router';

// import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown'
// import { SharedService } from '../../../services/shared.service';

// @Component({
//   selector: 'app-multiselect',
//   standalone: true,
//   imports: [
//     CommonModule,
//     NgMultiSelectDropDownModule
//   ],
//   templateUrl: './multiselect.component.html',
//   styleUrl: './multiselect.component.scss'
// })
// export class MultiselectComponent {

//   listRegions: any;
//   selectedItemsRegion: any[] = [];
//   regionSettings = {};
//   @Input() url!: string;

//   constructor(
//     private el: ElementRef,
//     private router: Router,

//     private sharedService: SharedService,


//   ) { }

//   ngOnInit(): void {
    
//     this.selectedItemsRegion = [];

//     this.regionSettings = {
//       singleSelection: true,
//       idField: 'regionId',
//       textField: 'regionName',
//       itemsShowLimit: 3,
//       allowSearchFilter: false,
//       enableCheckAll: false,
//     }

//   }


//       // Event drop down to select role
//       onDropDownCloseRegion() {
//         let myTag = this.el.nativeElement.querySelector("label.label-role");
//         if (this.selectedItemsRegion.length != 0) {
//           console.log(this.selectedItemsRegion.length);
//           myTag.classList.add('label_after');
//         } else {
//           myTag.classList.remove('label_after');
//         }
//         this.sharedService.url = this.url
//         this.sharedService.getAll()
//           .subscribe((regions: any) => this.listRegions = regions);
//         console.log("AfterView" + JSON.stringify(this.listRegions));
//       }
    
    
//       // select methods for role
//       onItemSelectRegion(item: any) {
//         this.selectedItemsRegion.push(item)
//         //console.log('form model', this.selectedItemsRegion);
//       }
    
//       onItemDeSelectRegion(item: any) {
//         this.selectedItemsRegion = this.selectedItemsRegion.filter(itm => itm.item_id !== item.item_id)
//         //console.log('form model', this.selectedItemsRegion);
//       }
// }
