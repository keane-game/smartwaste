import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { headerTitleService } from '../../../services/headerTitle.service';
import { environment } from '../../../../environments/environment';
import { API_ENDPOINTS, API_PATHS } from '../../../shared/constants/api-endpoints';

interface Commune {
  communeId: string;
  name: string;
}

type StopPriority = 'DEBORDEMENT' | 'ETAT_INCONNU' | 'A_SURVEILLER' | 'RIEN_A_FAIRE';

interface RouteStop {
  depotoirId: string;
  address: string;
  typeName: string;
  priority: StopPriority;
  fillLevelPercent: number | null;
  lastMeasuredAt: string | null;
  reason: string;
  latitude: number | null;
  longitude: number | null;
}

interface RouteCompletion {
  stops: number;
  served: number;
  collected: number;
}

const PRIORITY_META: Record<StopPriority, { label: string; badgeClass: string }> = {
  DEBORDEMENT: { label: 'Débordement', badgeClass: 'bg-danger' },
  ETAT_INCONNU: { label: 'État inconnu', badgeClass: 'bg-warning text-dark' },
  A_SURVEILLER: { label: 'À surveiller', badgeClass: 'bg-info text-dark' },
  RIEN_A_FAIRE: { label: 'Rien à faire', badgeClass: 'bg-secondary' },
};

const LAST_COMMUNE_KEY = 'collectionRoute.lastCommuneId';

@Component({
    selector: 'app-collection-route',
    templateUrl: './collection-route.component.html',
    styleUrls: ['./collection-route.component.scss'],
    standalone: false
})
export class CollectionRouteComponent implements OnInit {

  private readonly baseUrl = environment.apiUrl;

  communes: Commune[] = [];
  communeId: string | null = null;

  stops: RouteStop[] = [];
  completion: RouteCompletion | null = null;

  loading = false;
  errorMessage = '';
  actionInFlight: string | null = null;

  constructor(
    private http: HttpClient,
    private headerTitleService: headerTitleService,
  ) { }

  ngOnInit(): void {
    this.headerTitleService.setTitle('Tournée agent');
    this.http.get<Commune[]>(`${this.baseUrl}${API_ENDPOINTS.communes.listPath}`).subscribe({
      next: (communes) => {
        this.communes = communes;
        const stored = localStorage.getItem(LAST_COMMUNE_KEY);
        if (stored && communes.some(c => c.communeId === stored)) {
          this.communeId = stored;
          this.loadRoute();
        }
      },
      error: () => { this.errorMessage = "Impossible de charger la liste des communes."; }
    });
  }

  onCommuneChange(): void {
    if (this.communeId) {
      localStorage.setItem(LAST_COMMUNE_KEY, this.communeId);
    }
    this.loadRoute();
  }

  /** Part des points desservis, entre 0 et 1 — même formule que `Completion.rate()` côté backend. */
  get completionRate(): number {
    if (!this.completion || this.completion.stops === 0) {
      return 0;
    }
    return this.completion.served / this.completion.stops;
  }

  loadRoute(): void {
    if (!this.communeId) {
      return;
    }
    this.loading = true;
    this.errorMessage = '';
    const params = { communeId: this.communeId };

    forkJoin({
      stops: this.http.get<RouteStop[]>(`${this.baseUrl}${API_PATHS.collectionRoutes}`, { params }),
      completion: this.http.get<RouteCompletion>(`${this.baseUrl}${API_PATHS.collectionRouteCompletion}`, { params }),
    }).subscribe({
      next: ({ stops, completion }) => {
        this.stops = stops;
        this.completion = completion;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.status === 403
          ? "Vous n'êtes pas affecté à cette commune."
          : "Impossible de charger la tournée.";
      }
    });
  }

  priorityLabel(priority: StopPriority): string {
    return PRIORITY_META[priority]?.label ?? priority;
  }

  priorityBadgeClass(priority: StopPriority): string {
    return PRIORITY_META[priority]?.badgeClass ?? 'bg-secondary';
  }

  markCollected(stop: RouteStop): void {
    this.actionInFlight = stop.depotoirId;
    this.http.post(`${this.baseUrl}${API_PATHS.stopCollected(stop.depotoirId)}`, {}).subscribe({
      next: () => { this.actionInFlight = null; this.loadRoute(); },
      error: () => { this.actionInFlight = null; this.errorMessage = `Échec de la déclaration pour ${stop.address}.`; }
    });
  }

  markInaccessible(stop: RouteStop): void {
    const reason = prompt(`Motif d'inaccessibilité pour ${stop.address} :`);
    if (reason === null) {
      return; // annulé
    }
    this.actionInFlight = stop.depotoirId;
    this.http.post(`${this.baseUrl}${API_PATHS.stopInaccessible(stop.depotoirId)}`, { reason }).subscribe({
      next: () => { this.actionInFlight = null; this.loadRoute(); },
      error: () => { this.actionInFlight = null; this.errorMessage = `Échec de la déclaration pour ${stop.address}.`; }
    });
  }

}
