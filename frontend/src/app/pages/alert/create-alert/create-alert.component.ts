import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { SharedService } from '../../../services/shared.service';
import { first } from 'rxjs';
import { succesAlert, errorAlert } from '../../../services/alert.service';
import { API_ENDPOINTS } from '../../../shared/constants/api-endpoints';

@Component({
    selector: 'app-create-alert',
    templateUrl: './create-alert.component.html',
    styleUrl: './create-alert.component.scss',
    standalone: false
})
export class CreateAlertComponent {

  submitted = false;
  saving = false;
  alertForm!: FormGroup;
  currentAlert: any;
  id: any;
  readonly codes = ['WARNING', 'DANGER', 'INFO'];
  selectedFile?: File;
  /** Message affiché quand la géolocalisation échoue — l'alerte reste enregistrable sans. */
  locationNotice: string | null = null;

  constructor(
    private createAlertModal: MatDialogRef<CreateAlertComponent>,
    private formBuilder: FormBuilder,
    private sharedService: SharedService,
    @Inject(MAT_DIALOG_DATA) public data: any,
  ) {
    this.id = data?.id;
    this.currentAlert = data?.currentAlert;
  }

  ngOnInit(): void {
    this.alertForm = this.formBuilder.group({
      object: ['', Validators.required],
      code: ['', Validators.required],
      message: [null, Validators.required],
      address: [null, Validators.required],
      coordinate: this.formBuilder.group({
        longitude: [''],
        latitude: [''],
        altitude: [''],
      }),
    });

    if (this.id != undefined) {
      this.alertForm.patchValue(this.currentAlert);
    }
  }

  onFileSelected(event: any): void {
    this.selectedFile = event.target.files?.[0];
  }

  get f() { return this.alertForm.controls; }

  async onSubmit() {
    this.submitted = true;
    if (this.alertForm.invalid) {
      return;
    }

    // La géolocalisation est un ENRICHISSEMENT, pas un prérequis. Avant, `onSubmit` sortait en
    // erreur dès que `getCurrentLocation()` échouait : permission refusée, appareil sans GPS, ou
    // simplement page servie en HTTP (l'API n'est disponible qu'en contexte sécurisé) rendaient la
    // création d'alerte totalement impossible, sans recours. On tente de localiser, et si ça
    // échoue on enregistre quand même en le signalant — une alerte sans coordonnées reste utile
    // (le champ `coordinate` est facultatif côté backend, cf. DTO Alert).
    const position = await this.tryGetCurrentLocation();
    this.saving = true;

    // `setValue` sur le sous-groupe plutôt qu'une mutation de `alertForm.value` : `.value` est un
    // instantané reconstruit à chaque changement, y écrire ne touchait pas les contrôles et le
    // moindre recalcul écrasait les coordonnées.
    if (position) {
      this.alertForm.get('coordinate')!.setValue({
        latitude: String(position.coords.latitude),
        longitude: String(position.coords.longitude),
        altitude: position.coords.altitude != null ? String(position.coords.altitude) : '',
      });
    }

    const payload = { ...this.alertForm.value };
    if (!payload.coordinate?.latitude) {
      payload.coordinate = null;
    }

    const formData = new FormData();
    formData.append('alert', JSON.stringify(payload));
    // `formData.append('file', undefined)` sérialise la CHAÎNE "undefined" : le backend recevait
    // un `file` non vide qui n'était pas un fichier. On n'ajoute la partie que s'il y a un fichier
    // (`file` est `required = false` côté contrôleur).
    if (this.selectedFile) {
      formData.append('file', this.selectedFile);
    }

    this.sharedService.url = API_ENDPOINTS.alerts.basePath;
    const request$ = this.id != undefined
      ? this.sharedService.update(formData, this.id)
      : this.sharedService.create(formData);

    request$.pipe(first()).subscribe({
      next: () => {
        this.saving = false;
        succesAlert(this.id != undefined ? "L'alerte a bien été mise à jour" : "L'alerte a bien été créée");
        this.createAlertModal.close(true);
      },
      error: (error) => {
        this.saving = false;
        errorAlert('Erreur : ' + (error?.message ?? error));
      },
    });
  }

  /** Résout la position, ou `null` si indisponible/refusée — ne rejette jamais. */
  private tryGetCurrentLocation(): Promise<GeolocationPosition | null> {
    return new Promise((resolve) => {
      if (!navigator.geolocation) {
        this.locationNotice = "Géolocalisation non disponible : l'alerte sera enregistrée sans coordonnées.";
        resolve(null);
        return;
      }
      navigator.geolocation.getCurrentPosition(
        position => resolve(position),
        () => {
          this.locationNotice = "Position indisponible : l'alerte sera enregistrée sans coordonnées.";
          resolve(null);
        },
        { timeout: 5000 },
      );
    });
  }
}
