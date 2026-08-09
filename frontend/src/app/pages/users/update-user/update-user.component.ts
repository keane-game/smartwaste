import { Component, OnInit, ElementRef } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { ActivatedRoute, Router } from "@angular/router";
import { first } from "rxjs";
import { SharedService } from "../../../services/shared.service";
import { API_ENDPOINTS } from "../../../shared/constants/api-endpoints";

@Component({
    selector: 'app-update-user',
    templateUrl: './update-user.component.html',
    styleUrls: ['./update-user.component.scss'],
    standalone: false
})
export class UpdateUserComponent implements OnInit {

  isSelected: any = "label_after";
  selected: boolean = false;
  listRoles: any;
  selectedItemsRole: any[] = [];
  roleSettings = {};
  submitted = false;
  id: any;
  updateUserForm!: FormGroup;
  currentUser: any;



  constructor(
    private updateUserModal: MatDialogRef<UpdateUserComponent>,
    private el: ElementRef,
    private matDialog: MatDialog,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private sharedService: SharedService,
  ) { }

  ngOnInit(): void {

    this.updateUserForm = this.formBuilder.group({
      userFirstname: ['', Validators.required],
      userLastname: ['', Validators.required],
      userEmail: ['', Validators.required],
      password: [''],
      userId: [''],
      userAddress: ['', Validators.required],
      userPhone: ['', Validators.required],
      authority: [this.currentUser.authority, Validators.required],
    });
    const {authorityId, authorityName} = this.currentUser.authority;

    console.log(authorityId)
    this.currentUser.authority= {authorityId, authorityName};
    this.updateUserForm.patchValue(this.currentUser);
   
    this.selectedItemsRole = [];

    this.roleSettings = {
      singleSelection: true,
      idField: 'authorityId',
      textField: 'authorityName',
      itemsShowLimit: 3,
      allowSearchFilter: false,
      enableCheckAll: false,
    }
  }



   // Event drop down to select role
   onDropDownCloseRole() {
    // let myTag = this.el.nativeElement.querySelector("label.label-role");
    // if (this.selectedItemsRole.length != 0) {
    //   console.log(this.selectedItemsRole.length);
    //   myTag.classList.add('label_after');
    // } else {
    //   myTag.classList.remove('label_after');
    // }
    this.sharedService.url = API_ENDPOINTS.authorities.basePath;
    this.sharedService.getAll()
      .subscribe(roles => this.listRoles = roles);
    console.log("AfterView" + JSON.stringify(this.listRoles));
  }
 

  // select methods for role
  onItemSelectRole(item: any) {
    this.selectedItemsRole.push(item)
    console.log('form model', this.selectedItemsRole);
  }

  onItemDeSelectRole(item: any) {
    this.selectedItemsRole = this.selectedItemsRole.filter(itm => itm.item_id !== item.item_id)
    console.log('form model', this.selectedItemsRole);
  }

 

  // convenience getter for easy access to form fields
  get f() { return this.updateUserForm.controls; }

  onSubmit() {
    this.submitted = true;
   
    console.log(this.updateUserForm.value)
    // stop here if form is invalid
    this.sharedService.url = API_ENDPOINTS.users.basePath;
    this.sharedService.update(this.updateUserForm.value, this.id)
    .pipe(first())
    .subscribe({
      next:  () => {
      
        },
        error: error => {
          console.log(error)
          //this.alertService.error(error)
        }
    })
  }



  async reload(url: string): Promise<boolean> {
    await this.router.navigateByUrl('/', { skipLocationChange: true });
    return this.router.navigateByUrl(url);
  }
}

