import { Component, OnInit, ElementRef } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { ActivatedRoute, Router } from "@angular/router";
import { first } from "rxjs";
import { SharedService } from "src/app/services/shared.service";
import { PuPopWidget } from "src/app/shares/widget/pupop.widget";


@Component({
  selector: 'app-create-user',
  templateUrl: './create-user.component.html',
  styleUrls: ['./create-user.component.scss']
})
export class CreateUserComponent implements OnInit {

  isSelected: any = "label_after";
  selected: boolean = false;
  listRoles: any;
  listProjets: any;
  selectedItemsRole: any[] = [];
  selectedItemsProjet: any[] = [];
  projetSettings = {};
  roleSettings = {};
  submitted = false;
  fieldPrenom = "";
  fieldNom = "";
  userForm!: FormGroup;


  successDialogRef!: MatDialogRef<PuPopWidget>;
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
      fullName: ['', Validators.required],
      adressMail: ['', Validators.required],
      das: ['', Validators.required],
      roles: ['', Validators.required],
      projects: ['', Validators.required],
    });


    this.selectedItemsRole = [];
    this.selectedItemsProjet = [];

    this.roleSettings = {
      singleSelection: false,
      idField: 'id',
      textField: 'roleName',
      itemsShowLimit: 3,
      allowSearchFilter: false,
      enableCheckAll: false,
    }

    this.projetSettings = {
      singleSelection: false,
      idField: 'projectId',
      textField: 'projectName',
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
    this.successDialogRef = this.matDialog.open(PuPopWidget, {
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

    // this.roleService.getAllRoles()
    //   .subscribe(roles => this.listRoles = roles);
    //console.log("AfterView" + JSON.stringify(this.listRoles));
  }






  // convenience getter for easy access to form fields
  get f() { return this.userForm.controls; }

  onSubmit() {
    this.submitted = true;
    console.log(this.userForm.value)
    // reset alerts on submit
    //this.alertService.clear();
   
    this.CreateUser();

  }


  CreateUser() {
    this.userForm.value.fullName = `${this.fieldPrenom} ${this.fieldNom}`;;
    if (!this.userForm.invalid) {
      console.log(this.userForm.invalid)
      return;
    }
    this.sharedService.create(this.userForm.value)
      .pipe(first())
      .subscribe({
        next: () => {
          this.reload("/kpireview/users")
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
