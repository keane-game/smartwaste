import { Component, OnInit, ElementRef } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { ActivatedRoute, Router } from "@angular/router";
import { first } from "rxjs";
import { SharedService } from "src/app/services/shared.service";
import { PuPopWidget } from "src/app/shares/widget/pupop.widget";



@Component({
  selector: 'app-update-user',
  templateUrl: './update-user.component.html',
  styleUrls: ['./update-user.component.scss']
})
export class UpdateUserComponent implements OnInit {

  isSelected: any = "label_after";
  selected: boolean = false;
  listRoles: any;
  listProjets: any;
  selectedItemsRole: any[] = [];
  selectedItemsProjet: any[] = [];
  projetSettings = {};
  roleSettings = {};
  submitted = false;
  id: any;
  updateUserForm!: FormGroup;
  currentUser: any;
  fieldPrenom = "";
  fieldNom = "";


  successDialogRef!: MatDialogRef<PuPopWidget>;
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
      fullName: [this.currentUser.fullName, Validators.required],
      adressMail: [this.currentUser.adressMail, Validators.required],
      das: [this.currentUser.das, Validators.required],
      roles: [this.currentUser.roles, Validators.required],
      projects: [this.currentUser.projects, Validators.required],
    });
    this.spliteFullName(this.currentUser.fullName);

     
    this.selectedItemsRole = [];
    this.selectedItemsProjet = [];

    this.projetSettings = {
      singleSelection: false,
      idField: 'projectId',
      textField: 'projectName',
      itemsShowLimit: 3,
      allowSearchFilter: false,
      enableCheckAll: false,
    }

    this.roleSettings = {
      singleSelection: false,
      idField: 'id',
      textField: 'roleName',
      itemsShowLimit: 3,
      allowSearchFilter: false,
      enableCheckAll: false,
    }
  }

  CloseCreateUserModal() {
    this.updateUserModal.close(false)
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
    this.successDialogRef.componentInstance.message = "Utilisateur modifié avec succès";
  }


  // Event drop down to select role
  onDropDownCloseRole() {
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

  // select methods for projet
  onItemSelectProjet(item: any) {
    this.selectedItemsProjet.push(item)
    console.log('form model', this.selectedItemsProjet);
  }

  onItemDeSelectProjet(item: any) {
    this.selectedItemsProjet = this.selectedItemsProjet.filter(itm => itm.item_id !== item.item_id)
    console.log('form model', this.selectedItemsProjet);
  }

  // convenience getter for easy access to form fields
  get f() { return this.updateUserForm.controls; }

  onSubmit() {
    this.submitted = true;
    this.updateUserForm.value.fullName = `${this.fieldPrenom} ${this.fieldNom}`;;
   
    console.log(this.updateUserForm.value)
    // stop here if form is invalid
 
    this.sharedService.update(this.updateUserForm.value, this.id)
    .pipe(first())
    .subscribe({
      next:  () => {
        this.reload("/kpireview/users")
        this.CloseCreateUserModal()
        setTimeout(()=>  {
          //window.location.reload()
          this.CloseSuccessModal()
        }, 1500 );
       
        },
        error: error => {
          console.log(error)
          //this.alertService.error(error)
        }
    })
  }


  // splite user fullName to firstname and lastname 
  //and set it on fielNom and fielPrenom
  //

  spliteFullName(fullName: string){
    const fullNames = fullName.trim().split(" ");
    
    fullNames.forEach((element, index) => {
      if(index == fullNames.length-1)
        this.fieldNom = element;
      else
        this.fieldPrenom += element
      
    });

  }

  async reload(url: string): Promise<boolean> {
    await this.router.navigateByUrl('/', { skipLocationChange: true });
    return this.router.navigateByUrl(url);
  }
}

