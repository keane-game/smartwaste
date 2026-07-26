import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { first } from 'rxjs';
import { SharedService } from '../../../services/shared.service';
import { headerTitleService } from '../../../services/headerTitle.service';
import { AlertStreamService } from '../../../services/alert-stream.service';
import { environment } from '../../../../environments/environment';

/**
 * Gestion des alertes — liste + création/édition (avec image) + suppression.
 *
 * Backend `/v1/alerts` : la liste complète est servie par `GET /alertss` ; la suppression par
 * `DELETE /alerts/{id}`. La création/mise à jour se fait en **multipart** (champ `alert` =
 * JSON de l'alerte, partie `file` = image facultative), d'où l'appel `HttpClient` direct
 * plutôt que {@link SharedService} qui n'envoie que du JSON.
 */
@Component({
  selector: 'app-list-alert',
  templateUrl: './list-alert.component.html',
  styleUrls: ['./list-alert.component.scss']
})
export class ListAlertComponent implements OnInit, OnDestroy {

  private readonly resource = '/alerts';
  private readonly listUrl = '/alertss';

  readonly codes = ['WARNING', 'INFO', 'DANGER'];

  items: any[] = [];
  loading = false;
  saving = false;
  message = '';
  error = '';

  /** Alertes reçues en direct via SSE depuis l'ouverture de l'écran (ADR-0007). */
  liveCount = 0;
  liveMessage = '';

  form!: FormGroup;
  showForm = false;
  editingId: number | null = null;
  selectedFile: File | null = null;

  constructor(
    private http: HttpClient,
    private sharedService: SharedService,
    private formBuilder: FormBuilder,
    private headerTitleService: headerTitleService,
    private alertStream: AlertStreamService
  ) { }

  ngOnInit(): void {
    this.headerTitleService.setTitle('Gestion Alerte');
    this.form = this.formBuilder.group({
      object: ['', Validators.required],
      message: ['', Validators.required],
      address: [''],
      code: ['INFO', Validators.required]
    });
    this.load();

    // Flux temps réel : une alerte créée ailleurs (autre superviseur, ou à terme le moteur
    // de seuils IoT) apparaît sans rechargement ni polling.
    this.alertStream.alerts.subscribe(alert => {
      if (!alert) { return; }
      const id = alert.alertId;
      // Le créateur reçoit aussi son propre événement : on évite le doublon.
      if (id != null && this.items.some(i => i.alertId === id)) { return; }
      this.items = [alert, ...this.items];
      this.liveCount++;
      this.liveMessage = `Nouvelle alerte reçue en direct : ${alert.object ?? 'sans objet'}`;
    });
    this.alertStream.connect();
  }

  ngOnDestroy(): void {
    this.alertStream.disconnect();
  }

  get f() { return this.form.controls; }

  load(): void {
    this.loading = true;
    this.sharedService.url = this.listUrl;
    this.sharedService.getAll().subscribe({
      next: (data) => { this.items = data || []; this.loading = false; },
      error: () => { this.loading = false; this.error = 'Erreur de chargement des alertes.'; }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files && input.files.length ? input.files[0] : null;
  }

  openCreate(): void {
    this.editingId = null;
    this.selectedFile = null;
    this.form.reset({ code: 'INFO' });
    this.showForm = true;
    this.message = '';
    this.error = '';
  }

  openEdit(item: any): void {
    this.editingId = item.alertId;
    this.selectedFile = null;
    this.form.patchValue({
      object: item.object, message: item.message,
      address: item.address, code: item.code ?? 'INFO'
    });
    this.showForm = true;
    this.message = '';
    this.error = '';
  }

  cancel(): void {
    this.showForm = false;
    this.editingId = null;
    this.selectedFile = null;
    this.form.reset({ code: 'INFO' });
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;

    const formData = new FormData();
    formData.append('alert', JSON.stringify(this.form.value));
    if (this.selectedFile) { formData.append('file', this.selectedFile); }

    const request = this.editingId
      ? this.http.put(`${environment.apiUrl}${this.resource}/${this.editingId}`, formData)
      : this.http.post(`${environment.apiUrl}${this.resource}`, formData);

    request.pipe(first()).subscribe({
      next: () => {
        this.saving = false;
        this.message = this.editingId ? 'Alerte mise à jour.' : 'Alerte créée.';
        this.cancel();
        this.load();
      },
      error: () => { this.saving = false; this.error = 'Échec de l\'enregistrement.'; }
    });
  }

  remove(item: any): void {
    if (!confirm(`Supprimer l'alerte « ${item.object} » ?`)) { return; }
    this.sharedService.url = this.resource;
    this.sharedService.delete(item.alertId).subscribe({
      next: () => { this.message = 'Alerte supprimée.'; this.load(); },
      error: () => { this.error = 'Échec de la suppression.'; }
    });
  }
}
