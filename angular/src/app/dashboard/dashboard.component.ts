import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
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
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  private readonly dataBase = environment.apiUrl.replace(/\/v1\/?$/, '');

  loading = false;
  error = '';
  state: any = {};

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
