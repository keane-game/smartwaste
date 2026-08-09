import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { headerTitleService } from '../../../services/headerTitle.service';
import { environment } from '../../../../environments/environment';
import { API_ENDPOINTS, API_PATHS } from '../../../shared/constants/api-endpoints';

interface Commune {
  communeId: string;
  name: string;
}

interface ChronicPoint {
  depotoirId: string;
  address: string;
  overflowCount: number;
}

interface PerformanceReport {
  communeId: string | null;
  communeName: string | null;
  from: string;
  to: string;
  alertsRaised: number;
  alertsResolved: number;
  averageResolutionHours: number | null;
  stops: number;
  served: number;
  collected: number;
  inaccessible: number;
  completionRate: number;
  chronicPoints: ChronicPoint[];
}

@Component({
    selector: 'app-reports',
    templateUrl: './reports.component.html',
    styleUrls: ['./reports.component.scss'],
    standalone: false
})
export class ReportsComponent implements OnInit {

  private readonly baseUrl = environment.apiUrl;

  communes: Commune[] = [];
  communeId: string | null = null;
  /** Champs `<input type="date">` — au format `yyyy-MM-dd`, convertis en ISO-8601 à l'appel. */
  from: string | null = null;
  to: string | null = null;

  report: PerformanceReport | null = null;
  loading = false;
  downloadingCsv = false;
  errorMessage = '';

  constructor(
    private http: HttpClient,
    private headerTitleService: headerTitleService,
  ) { }

  ngOnInit(): void {
    this.headerTitleService.setTitle('Rapports');
    this.http.get<Commune[]>(`${this.baseUrl}${API_ENDPOINTS.communes.listPath}`).subscribe({
      next: (communes) => { this.communes = communes; },
      error: () => { this.errorMessage = "Impossible de charger la liste des communes."; }
    });
    this.generate();
  }

  private buildParams(): HttpParams {
    let params = new HttpParams();
    if (this.communeId) {
      params = params.set('communeId', this.communeId);
    }
    // `<input type="date">` rend une date locale sans heure ; on la borne au début/à la fin de
    // journée pour que la période couvre bien le jour choisi, pas seulement minuit.
    if (this.from) {
      params = params.set('from', new Date(`${this.from}T00:00:00`).toISOString());
    }
    if (this.to) {
      params = params.set('to', new Date(`${this.to}T23:59:59`).toISOString());
    }
    return params;
  }

  generate(): void {
    this.loading = true;
    this.errorMessage = '';
    this.http.get<PerformanceReport>(`${this.baseUrl}${API_PATHS.supervisionReports}`, { params: this.buildParams() })
      .subscribe({
        next: (report) => { this.report = report; this.loading = false; },
        error: (err) => {
          this.loading = false;
          this.errorMessage = err.status === 400
            ? "La fin de période doit être postérieure au début."
            : "Impossible de générer le rapport.";
        }
      });
  }

  downloadCsv(): void {
    this.downloadingCsv = true;
    this.http.get(`${this.baseUrl}${API_PATHS.supervisionReportsCsv}`, {
      params: this.buildParams(),
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        this.downloadingCsv = false;
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'rapport-collecte.csv';
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => {
        this.downloadingCsv = false;
        this.errorMessage = "Impossible de télécharger le rapport CSV.";
      }
    });
  }

}
