import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { first } from 'rxjs';
import { headerTitleService } from '../../services/headerTitle.service';
import { environment } from '../../../environments/environment';

interface UploadTarget {
  key: string;    // segment de l'endpoint /data/{key}
  label: string;
  file: File | null;
  busy: boolean;
  result: string;
  error: string;
}

/**
 * Import de données — deux mécanismes exposés par le backend :
 *
 * 1. Import des GeoJSON de référence (`POST /v1/admin/import/geojson?force=`) : le serveur
 *    charge `datas/*.json` en base et renvoie un récapitulatif par étape (authentifié).
 * 2. Upload de fichiers par ressource (`POST /data/{commune|department|quartier|
 *    circuitcollect|circuitbalayage|depotoir}`, multipart `file`).
 *
 * `/data/**` est hors du préfixe `/v1`, d'où la base dérivée depuis {@link environment.apiUrl}.
 */
@Component({
  selector: 'app-data-import',
  templateUrl: './data-import.component.html',
  styleUrls: ['./data-import.component.scss']
})
export class DataImportComponent implements OnInit {

  private readonly base = environment.apiUrl;                 // .../v1
  private readonly dataBase = environment.apiUrl.replace(/\/v1\/?$/, '');

  // --- Import GeoJSON de référence ---
  force = false;
  refBusy = false;
  refError = '';
  refSummary: { step: string; outcome: string }[] = [];

  // --- Uploads par ressource ---
  targets: UploadTarget[] = [
    { key: 'department', label: 'Départements', file: null, busy: false, result: '', error: '' },
    { key: 'commune', label: 'Communes', file: null, busy: false, result: '', error: '' },
    { key: 'quartier', label: 'Quartiers', file: null, busy: false, result: '', error: '' },
    { key: 'depotoir', label: 'Dépotoirs', file: null, busy: false, result: '', error: '' },
    { key: 'circuitcollect', label: 'Circuits de collecte', file: null, busy: false, result: '', error: '' },
    { key: 'circuitbalayage', label: 'Circuits de balayage', file: null, busy: false, result: '', error: '' }
  ];

  constructor(
    private http: HttpClient,
    private headerTitleService: headerTitleService
  ) { }

  ngOnInit(): void {
    this.headerTitleService.setTitle('Import de données');
  }

  runReferenceImport(): void {
    this.refBusy = true;
    this.refError = '';
    this.refSummary = [];
    this.http.post<Record<string, string>>(
      `${this.base}/admin/import/geojson?force=${this.force}`, {}
    ).pipe(first()).subscribe({
      next: (summary) => {
        this.refBusy = false;
        this.refSummary = Object.entries(summary || {}).map(([step, outcome]) => ({ step, outcome }));
      },
      error: () => { this.refBusy = false; this.refError = 'Échec de l\'import GeoJSON de référence.'; }
    });
  }

  onFileSelected(target: UploadTarget, event: Event): void {
    const input = event.target as HTMLInputElement;
    target.file = input.files && input.files.length ? input.files[0] : null;
    target.result = '';
    target.error = '';
  }

  upload(target: UploadTarget): void {
    if (!target.file) { target.error = 'Sélectionnez un fichier.'; return; }
    target.busy = true;
    target.result = '';
    target.error = '';
    const formData = new FormData();
    formData.append('file', target.file);
    this.http.post(`${this.dataBase}/data/${target.key}`, formData, { responseType: 'text' })
      .pipe(first()).subscribe({
        next: (res) => { target.busy = false; target.result = res || 'Import effectué.'; target.file = null; },
        error: () => { target.busy = false; target.error = 'Échec de l\'import.'; }
      });
  }
}
