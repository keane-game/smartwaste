import { Component, Inject, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { first } from "rxjs";
import { SharedService } from "../../../services/shared.service";
import { succesAlert, errorAlert } from "../../../services/alert.service";
import { API_ENDPOINTS } from "../../../shared/constants/api-endpoints";

/**
 * Création / modification d'un compte.
 *
 * <p><b>Le mot de passe est désormais saisi.</b> Le formulaire n'avait aucun champ pour lui, mais
 * le contrôle valait {@code 'user'} EN DUR — et `UserServiceImpl.createUser` hache ce qu'on lui
 * soumet. Tout compte créé depuis cet écran recevait donc le mot de passe « user ». Il est
 * maintenant demandé, et exigé uniquement à la création.
 *
 * <p><b>Il n'est jamais renvoyé à la modification.</b> Le backend ne recopie pas le mot de passe
 * lors d'un `PUT` (il ne fait suivre que les champs non nuls qu'il liste explicitement), donc rien
 * n'était cassé ; mais transporter un mot de passe en clair sur une requête qui n'en a pas besoin
 * n'a aucune raison d'être.
 *
 * <p>Le rôle passe d'un `ng-multiselect-dropdown` à un `select` simple : le composant rendait une
 * valeur en TABLEAU, d'où le `authority[0]` du code d'origine — qui redevenait `undefined` en
 * modification, puisque `patchValue` y injectait l'objet `authority` de l'API, pas un tableau.
 */
@Component({
    selector: 'app-create-user',
    templateUrl: './create-user.component.html',
    styleUrls: ['./create-user.component.scss'],
    standalone: false
})
export class CreateUserComponent implements OnInit {

  submitted = false;
  saving = false;
  userForm!: FormGroup;
  currentUser: any;
  id: any;
  listRoles: any[] = [];

  constructor(
    private createUserModal: MatDialogRef<CreateUserComponent>,
    private formBuilder: FormBuilder,
    private sharedService: SharedService,
    @Inject(MAT_DIALOG_DATA) public data: any,
  ) {
    this.id = data?.id;
    this.currentUser = data?.currentUser;
  }

  get isEdit(): boolean {
    return this.id != undefined;
  }

  ngOnInit(): void {
    this.userForm = this.formBuilder.group({
      userFirstname: ['', Validators.required],
      userLastname: ['', Validators.required],
      userEmail: ['', [Validators.required, Validators.email]],
      // Requis à la création seulement : à la modification le champ n'est pas affiché et le mot
      // de passe n'est pas transmis.
      userPassword: ['', this.isEdit ? [] : [Validators.required, Validators.minLength(8)]],
      userAddress: [''],
      userPhone: [''],
      authorityId: [null, Validators.required],
      // Coché par défaut : un compte créé ici est verrouillé sans cela (`isAccountNonLocked()`
      // renvoie `activated`), et la connexion échoue sur « User account is locked ». Le parcours
      // d'activation par e-mail ne dessert que l'inscription publique — un compte ouvert par un
      // administrateur, dont il transmet lui-même le mot de passe, n'y passe jamais.
      // `updateUser` ne recopie pas ce champ : il ne vaut donc qu'à la création.
      activated: [true],
    });

    if (this.isEdit) {
      this.userForm.patchValue({
        ...this.currentUser,
        // L'API expose le rôle sous `authority.name`/`authority.authorityId` ; le formulaire ne
        // manipule que l'identifiant.
        authorityId: this.currentUser?.authority?.authorityId ?? null,
      });
    }

    this.sharedService.url = API_ENDPOINTS.authorities.listPath;
    this.sharedService.getAll().subscribe(roles => this.listRoles = roles ?? []);
  }

  // convenience getter for easy access to form fields
  get f() { return this.userForm.controls; }

  onSubmit() {
    this.submitted = true;
    // La garde était COMMENTÉE dans la version d'origine : un formulaire incomplet partait quand
    // même au serveur, qui répondait par une erreur peu parlante.
    if (this.userForm.invalid) {
      return;
    }
    this.saving = true;

    const { userPassword, authorityId, ...rest } = this.userForm.value;
    const payload: any = { ...rest, authority: { authorityId } };
    if (!this.isEdit) {
      payload.userPassword = userPassword;
    }

    this.sharedService.url = API_ENDPOINTS.users.basePath;
    const request$ = this.isEdit
      ? this.sharedService.update(payload, this.id)
      : this.sharedService.create(payload);

    request$.pipe(first()).subscribe({
      next: () => {
        this.saving = false;
        succesAlert(this.isEdit ? 'Le compte a bien été mis à jour' : 'Le compte a bien été créé');
        this.createUserModal.close(true);
      },
      error: (error) => {
        this.saving = false;
        errorAlert('Erreur : ' + (error?.message ?? error));
      },
    });
  }
}
