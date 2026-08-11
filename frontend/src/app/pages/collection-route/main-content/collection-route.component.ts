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
  DEBORDEMENT: { label: 'Débordement', badgeClass: 'bg-destructive/10 text-destructive' },
  ETAT_INCONNU: { label: 'État inconnu', badgeClass: 'bg-warning/10 text-warning' },
  A_SURVEILLER: { label: 'À surveiller', badgeClass: 'bg-primary/10 text-primary' },
  RIEN_A_FAIRE: { label: 'Rien à faire', badgeClass: 'bg-muted-surface text-muted' },
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

  /** Point dont on saisit le motif d'inaccessibilité (`null` = aucune saisie en cours). */
  reasonForStopId: string | null = null;
  reasonText = '';

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
    return PRIORITY_META[priority]?.badgeClass ?? 'bg-muted-surface text-muted';
  }

  markCollected(stop: RouteStop): void {
    this.actionInFlight = stop.depotoirId;
    this.http.post(`${this.baseUrl}${API_PATHS.stopCollected(stop.depotoirId)}`, {}).subscribe({
      next: () => { this.actionInFlight = null; this.loadRoute(); },
      error: () => { this.actionInFlight = null; this.errorMessage = `Échec de la déclaration pour ${stop.address}.`; }
    });
  }

  /**
   * Ouvre la saisie du motif sous le point concerné.
   *
   * <p>Remplace un `prompt()` natif : sur mobile — le terrain, donc le cas normal pour un agent —
   * il s'affiche en surcouche système, tronque les libellés longs, et certains navigateurs le
   * bloquent purement et simplement. Une zone de saisie dans la carte reste visible à côté de
   * l'adresse concernée.
   */
  askInaccessible(stop: RouteStop): void {
    this.reasonForStopId = stop.depotoirId;
    this.reasonText = '';
  }

  cancelInaccessible(): void {
    this.reasonForStopId = null;
    this.reasonText = '';
  }

  confirmInaccessible(stop: RouteStop): void {
    const reason = this.reasonText.trim();
    if (!reason) {
      return;
    }
    this.actionInFlight = stop.depotoirId;
    this.http.post(`${this.baseUrl}${API_PATHS.stopInaccessible(stop.depotoirId)}`, { reason }).subscribe({
      next: () => {
        this.actionInFlight = null;
        this.cancelInaccessible();
        this.loadRoute();
      },
      error: () => { this.actionInFlight = null; this.errorMessage = `Échec de la déclaration pour ${stop.address}.`; }
    });
  }

  /** Couleur du niveau de remplissage — même seuils que la carte du tableau de bord. */
  fillLevelClass(percent: number | null): string {
    if (percent === null) { return 'text-muted'; }
    if (percent >= 80) { return 'text-destructive font-semibold'; }
    if (percent >= 50) { return 'text-warning font-semibold'; }
    return 'text-success';
  }
}
