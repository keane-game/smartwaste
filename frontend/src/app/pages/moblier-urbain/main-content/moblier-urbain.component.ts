import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { headerTitleService } from '../../../services/headerTitle.service';
import { environment } from '../../../../environments/environment';
import { API_ENDPOINTS } from '../../../shared/constants/api-endpoints';
import { succesAlert, errorAlert } from '../../../services/alert.service';

interface MoblierUrbain {
  moblierUrbainId: string;
  name: string;
  code: string;
}

@Component({
    selector: 'app-moblier-urbain',
    templateUrl: './moblier-urbain.component.html',
    styleUrls: ['./moblier-urbain.component.scss'],
    standalone: false
})
export class MoblierUrbainComponent implements OnInit {

  private readonly baseUrl = `${environment.apiUrl}${API_ENDPOINTS['moblier-urbains'].basePath}`;

  items: MoblierUrbain[] = [];
  loading = false;
  submitting = false;

  /** `null` = formulaire de création ; sinon id de l'élément en cours de modification. */
  editingId: string | null = null;
  form = { name: '', code: '' };

  constructor(
    private http: HttpClient,
    private headerTitleService: headerTitleService,
  ) { }

  ngOnInit(): void {
    this.headerTitleService.setTitle('Mobiliers urbains');
    this.load();
  }

  load(): void {
    this.loading = true;
    this.http.get<MoblierUrbain[]>(this.baseUrl).subscribe({
      next: (items) => { this.items = items; this.loading = false; },
      error: () => { this.loading = false; errorAlert('Impossible de charger les mobiliers urbains.'); }
    });
  }

  startEdit(item: MoblierUrbain): void {
    this.editingId = item.moblierUrbainId;
    this.form = { name: item.name, code: item.code };
  }

  cancelEdit(): void {
    this.editingId = null;
    this.form = { name: '', code: '' };
  }

  submit(): void {
    if (!this.form.name.trim() || !this.form.code.trim()) {
      return;
    }
    this.submitting = true;
    const request$ = this.editingId === null
      ? this.http.post<MoblierUrbain>(this.baseUrl, this.form)
      : this.http.put<MoblierUrbain>(`${this.baseUrl}/${this.editingId}`, this.form);

    request$.subscribe({
      next: () => {
        this.submitting = false;
        succesAlert(this.editingId === null ? 'Mobilier urbain créé.' : 'Mobilier urbain modifié.');
        this.cancelEdit();
        this.load();
      },
      error: () => {
        this.submitting = false;
        errorAlert("Échec de l'enregistrement.");
      }
    });
  }

  remove(item: MoblierUrbain): void {
    if (!confirm(`Supprimer « ${item.name} » ?`)) {
      return;
    }
    this.http.delete(`${this.baseUrl}/${item.moblierUrbainId}`, { responseType: 'text' }).subscribe({
      next: () => { succesAlert('Mobilier urbain supprimé.'); this.load(); },
      error: () => { errorAlert('Échec de la suppression.'); }
    });
  }

}
