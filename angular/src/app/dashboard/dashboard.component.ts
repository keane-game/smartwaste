import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { headerTitleService } from '../services/headerTitle.service';
import { environment } from '../../environments/environment';

interface StatCard {
  key: string;
  label: string;
  hint: string;
  icon: string;
}

/**
 * Tableau de bord — indicateurs de supervision réels.
 *
 * Remplace l'ancien gabarit statique (Chart.js codé en dur) par les compteurs servis par
 * `GET /data/departmentState` ({@code DepartmentState}). L'endpoint `/data` est hors du préfixe
 * `/v1`, d'où la base dérivée depuis {@link environment.apiUrl}.
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  private readonly dataBase = environment.apiUrl.replace(/\/v1\/?$/, '');

  loading = false;
  error = '';
  state: any = {};

  /** Indicateurs avancés (P2-5) — GET /v1/supervision/stats. */
  stats: any = null;
  statsError = '';
  windowDays = 30;

  /** Correspondance identifiant de commune -> nom, pour libeller les agrégats. */
  private communeNames: Record<string, string> = {};

  readonly cards: StatCard[] = [
    { key: 'totalHabitants', label: 'Habitants', hint: 'Population supervisée', icon: 'bi-people' },
    { key: 'totalCommunes',  label: 'Communes', hint: 'Communes couvertes', icon: 'bi-geo-alt' },
    { key: 'totalDepotoirs', label: 'Dépotoirs', hint: 'Points de dépôt', icon: 'bi-trash' },
    { key: 'totalCircuits',  label: 'Circuits', hint: 'Circuits de collecte', icon: 'bi-signpost-2' },
    { key: 'totalBacs',      label: 'Bacs', hint: 'Bacs de rue', icon: 'bi-box' },
    { key: 'totalBennes',    label: 'Bennes', hint: 'Bennes déployées', icon: 'bi-truck' },
    { key: 'totalPP',        label: 'Points propres', hint: 'Points propres (PP)', icon: 'bi-recycle' },
    { key: 'totalCP',        label: 'Points de collecte', hint: 'Points de collecte (CP)', icon: 'bi-pin-map' },
    { key: 'totalPRN',       label: 'PRN', hint: 'Points de regroupement', icon: 'bi-diagram-3' }
  ];

  constructor(
    private http: HttpClient,
    private headerTitleService: headerTitleService
  ) { }

  ngOnInit(): void {
    this.headerTitleService.setTitle('Dashboard');
    this.loadState();
    this.loadStats();
    this.loadCommuneNames();
  }

  /** Charge les communes pour traduire les identifiants des agrégats en noms lisibles. */
  private loadCommuneNames(): void {
    this.http.get<any[]>(`${environment.apiUrl}/communes/s`).subscribe({
      next: rows => {
        (rows || []).forEach(c => { this.communeNames[String(c.communeId)] = c.name; });
      },
      error: () => { /* non bloquant : on retombe sur l'identifiant */ }
    });
  }

  /** Nom d'une commune depuis son identifiant, avec repli sur « Commune #id ». */
  communeName(key: string): string {
    return this.communeNames[key] || `Commune #${key}`;
  }

  /** Nombre d'alertes du jour le plus chargé de la fenêtre, pour l'échelle du graphique. */
  get peakAlerts(): number {
    return Math.max(0, ...(this.stats?.alertsPerDay ?? []).map((d: any) => d.count));
  }

  /**
   * Indicateurs avancés de supervision. Chargés séparément des compteurs `/data` : ils
   * viennent d'un endpoint authentifié (`/v1/**`), et leur indisponibilité ne doit pas
   * masquer les compteurs de base.
   */
  loadStats(): void {
    this.statsError = '';
    this.http.get<any>(`${environment.apiUrl}/supervision/stats`, { params: { windowDays: this.windowDays } })
      .subscribe({
        next: data => { this.stats = data; },
        error: () => { this.statsError = 'Indicateurs avancés indisponibles.'; }
      });
  }

  /** Hauteur relative d'une barre du graphique alertes/jour, en pourcentage. */
  barHeight(count: number): number {
    const max = Math.max(1, ...(this.stats?.alertsPerDay ?? []).map((d: any) => d.count));
    return Math.round((count / max) * 100);
  }

  /** Transforme une Map backend en paires triées, pour l'affichage. */
  entries(map: any): { key: string; value: number }[] {
    if (!map) { return []; }
    return Object.keys(map).map(k => ({ key: k, value: map[k] })).sort((a, b) => b.value - a.value);
  }

  get hasPendingDeletions(): boolean {
    return this.entries(this.stats?.pendingDeletions).length > 0;
  }

  loadState(): void {
    this.loading = true;
    this.error = '';
    this.http.get<any>(`${this.dataBase}/data/departmentState`).subscribe({
      next: (data) => { this.state = data || {}; this.loading = false; },
      error: () => { this.loading = false; this.error = 'Impossible de charger les indicateurs.'; }
    });
  }
}
