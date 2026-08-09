import { Component, OnInit, ElementRef, Inject } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from "@angular/material/dialog";
import { ActivatedRoute, Router } from "@angular/router";
import { first } from "rxjs";
import { SharedService } from "../../../services/shared.service";
import { succesAlert, errorAlert } from "../../../services/alert.service";


@Component({
    selector: 'app-create-user',
    templateUrl: './create-user.component.html',
    styleUrls: ['./create-user.component.scss'],
    standalone: false
})
export class CreateUserComponent implements OnInit {

  isSelected: any = "label_after";
  selected: boolean = false;
  listRoles: any;
  selectedItemsRole: any[] = [];
  roleSettings = {};
  submitted = false;
  userForm!: FormGroup;
  currentUser: any;
  id: any;
  
  constructor(
    private createUserModal: MatDialogRef<CreateUserComponent>,
    private el: ElementRef,
    private matDialog: MatDialog,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private sharedService: SharedService,

    @Inject(MAT_DIALOG_DATA) public data:any,
  ) { 
    this.id = data.id
    this.currentUser = data.currentUser
    console.log(data)
  }

  ngOnInit(): void {
    this.userForm = this.formBuilder.group({
      userFirstname: ['', Validators.required],
      userLastname: ['', Validators.required],
      userEmail: ['', Validators.required],
      userPassword: ['user', Validators.required],
      userAddress: ['', Validators.required],
      userPhone: ['', Validators.required],
      authority: [null, Validators.required],
    });


    console.log(this.id);
    if (this.id != undefined) {
      this.userForm.patchValue(this.currentUser);
    }

    this.selectedItemsRole = [];

    this.roleSettings = {
      singleSelection: true,
      idField: 'authorityId',
      textField: 'name',
      itemsShowLimit: 3,
      allowSearchFilter: false,
      enableCheckAll: false,
    }

  }



  // Event drop down to select role
  onDropDownCloseRole() {
    let myTag = this.el.nativeElement.querySelector("label.label-role");
    if (this.selectedItemsRole.length != 0) {
      console.log(this.selectedItemsRole.length);
      myTag.classList.add('label_after');
    } else {
      myTag.classList.remove('label_after');
    }
    this.sharedService.url = "/authorities"
    this.sharedService.getAll()
      .subscribe(roles => this.listRoles = roles);
    console.log("AfterView" + JSON.stringify(this.listRoles));
  }


  // select methods for role
  onItemSelectRole(item: any) {
    this.selectedItemsRole.push(item)
    //console.log('form model', this.selectedItemsRole);
  }

  onItemDeSelectRole(item: any) {
    this.selectedItemsRole = this.selectedItemsRole.filter(itm => itm.item_id !== item.item_id)
    //console.log('form model', this.selectedItemsRole);
  }




  // convenience getter for easy access to form fields
  get f() { return this.userForm.controls; }

  onSubmit() {
    this.submitted = true;
    this.sharedService.url = '/users';
    console.log(this.userForm.value)
    // this.userForm.value.fullName = `${this.fieldPrenom} ${this.fieldNom}`;;
    // if (this.userForm.invalid) {
    //   console.log(this.userForm.invalid)
    //   return;
    // }

    if (this.id != undefined){

      this.updateUser();
      console.log(this.userForm.invalid)
    }
      
    else
      this.createUser();


  }


  createUser() {
 
    this.userForm.value.authority = this.userForm.value.authority[0];
    this.sharedService.create(this.userForm.value)
    .pipe(first())
    .subscribe({
      next:  () => {
        succesAlert("La creation a bien réussie")
      },
      error: (error) => {
        errorAlert('Erreur' + error.message)
      }
    })
  }


  updateUser() {

    this.userForm.value.authority = this.userForm.value.authority[0];
    this.sharedService.update(this.userForm.value, this.id)
    .pipe(first())
      .subscribe({
        next:  () => {
          succesAlert("La mise à jour a bien réussie")
        },
        error: (error) => {
          errorAlert('Erreur' + error.message)
        }
      })
  }

  async reload(url: string): Promise<boolean> {
    await this.router.navigateByUrl('/', { skipLocationChange: true });
    console.log(this.userForm.invalid)
    return this.router.navigateByUrl(url);
  }

}
