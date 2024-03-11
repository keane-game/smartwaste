import { Component, OnInit, ElementRef } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { ActivatedRoute, Router } from "@angular/router";
import { first } from "rxjs";
import { SharedService } from "../../../services/shared.service";
import { SuccessComponent } from "../../../shares/success/success.component";


@Component({
  selector: 'app-create-user',
  templateUrl: './create-user.component.html',
  styleUrls: ['./create-user.component.scss']
})
export class CreateUserComponent implements OnInit {

  isSelected: any = "label_after";
  selected: boolean = false;
  listRoles: any;
  selectedItemsRole: any[] = [];
  roleSettings = {};
  submitted = false;
  userForm!: FormGroup;


  successDialogRef!: MatDialogRef<SuccessComponent>;
  constructor(
    private createUserModal: MatDialogRef<CreateUserComponent>,
    private el: ElementRef,
    private matDialog: MatDialog,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private sharedService: SharedService,

  ) { }

  ngOnInit(): void {
    this.userForm = this.formBuilder.group({
      userFirstname: ['', Validators.required],
      userLastname: ['', Validators.required],
      userEmail: ['', Validators.required],
      password: ['', Validators.required],
      userAddress: ['', Validators.required],
      userPhone: ['', Validators.required],
      authority: [null, Validators.required],
    });


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

  CloseCreateUserModal() {
    this.createUserModal.close(false)
    this.OpenSuccessModal()
  }
  
  CloseSuccessModal() {
    this.successDialogRef.close();
  }

  OpenSuccessModal() {
    this.successDialogRef = this.matDialog.open(SuccessComponent, {
      disableClose: false,
      panelClass: ['success-with-dialog'],
    });
    this.successDialogRef.componentInstance.message = "Utilisateur créé avec succès";
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
    this.sharedService.url = "/authority"
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
    this.sharedService.url = '/user';
    console.log(this.userForm.value)
    // reset alerts on submit
    //this.alertService.clear();
   
    this.CreateUser();

  }


  CreateUser() {
    // this.userForm.value.fullName = `${this.fieldPrenom} ${this.fieldNom}`;;
    if (!this.userForm.invalid) {
      console.log(this.userForm.invalid)
      return;
    }
    this.userForm.value.authority = this.userForm.value.authority[0];
    this.sharedService.create(this.userForm.value)
      .pipe(first())
      .subscribe({
        next: () => {
          this.reload("/users")
          this.CloseCreateUserModal()
          setTimeout(() => {
           // window.location.reload()
            this.CloseSuccessModal()
          }, 1500);

        },
        error: error => {
          console.log(error)
        }
      })
  }

  async reload(url: string): Promise<boolean> {
    await this.router.navigateByUrl('/', { skipLocationChange: true });
    console.log(this.userForm.invalid)
    return this.router.navigateByUrl(url);
  }

}
