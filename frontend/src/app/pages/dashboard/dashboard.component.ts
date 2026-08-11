import { ChangeDetectorRef, Component, ElementRef, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { headerTitleService } from '../../services/headerTitle.service';
import { Chart } from 'chart.js/auto';
import { imgDashboardConstant } from '../../shared/constants/images.constant';
import { SharedService } from '../../services/shared.service';
import { API_PATHS } from '../../shared/constants/api-endpoints';
import { environment } from '../../../environments/environment';
import { AlertStreamService } from '../../services/alert-stream.service';
import { DashboardMapComponent } from './dashboard-map/dashboard-map.component';

/** Sous-ensemble de `SupervisionStats` (backend) utilisé par ce tableau de bord. */
interface SupervisionStats {
  alertsPerDay: { day: string; count: number }[];
  alertsByCode: Record<string, number>;
  depotoirsByFillLevel: Record<string, number>;
  depotoirsByType: Record<string, number>;
  totalAlerts: number;
  alertsLast7Days: number;
  liveSubscribers: number;
  ingestion: {
    silentSensors: unknown[];
  };
  citizenReports: {
    byStatus: Record<string, number>;
    medianResolutionMinutes: number | null;
    openOlderThanThreshold: number;
  };
}

/** Une ligne du fil d'activité — la même alerte que le toast, mais qui reste consultable. */
interface ActivityItem {
  label: string;
  detail: string;
  code: string;
  at: Date;
}

/** Entrée de légende maison (pastille + libellé + valeur), comme dans les maquettes. */
export interface LegendEntry {
  label: string;
  value: number;
  color: string;
  /** Part du total, en pourcentage arrondi — affichée par les camemberts. */
  percent?: number;
}

@Component({
    selector: 'app-dashboard',
    imports: [CommonModule, DashboardMapComponent],
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnDestroy {

  departmentState: any;
  supervisionStats: SupervisionStats | null = null;
  imgConstants = imgDashboardConstant;
  /** Les plus récentes en tête ; bornée pour ne pas grossir indéfiniment sur une session longue. */
  activity: ActivityItem[] = [];
  private static readonly MAX_ACTIVITY_ITEMS = 8;

  @ViewChild('alertsChart') alertsChart!: ElementRef<HTMLCanvasElement>;
  @ViewChild('alertsByCodeChart') alertsByCodeChart!: ElementRef<HTMLCanvasElement>;
  @ViewChild('typeChart') typeChart!: ElementRef<HTMLCanvasElement>;

  /**
   * Légendes rendues en HTML plutôt que par Chart.js.
   *
   * <p>Les maquettes placent la légende À CÔTÉ du graphe, alignée à gauche, avec la valeur ou le
   * pourcentage — ce que la légende intégrée de Chart.js ne sait pas faire sans contorsions. Elle
   * est donc désactivée sur chaque graphe et reconstruite ici, ce qui la rend aussi lisible par
   * un lecteur d'écran (le canvas, lui, ne l'est pas).
   */
  fillLevelLegend: LegendEntry[] = [];
  codeLegend: LegendEntry[] = [];
  typeLegend: LegendEntry[] = [];
  totalDepotoirs = 0;

  /** Palette : vert de marque en tête, puis ambre/rouge pour la gravité, gris pour l'inconnu. */
  private static readonly PALETTE = ['#15803D', '#22C55E', '#D97706', '#DC2626', '#0EA5E9', '#94A3B8'];

  // `Chart<any>` : chaque graphe a un type concret (`line`, `doughnut`) que le type générique
  // `Chart` n'accepte pas en paramètre — on ne fait que les conserver pour les détruire.
  private readonly charts: Chart<any>[] = [];
  private alertsSubscription?: Subscription;

  constructor(
    private headerTitleService: headerTitleService,
    private sharedService: SharedService,
    private http: HttpClient,
    private alertStreamService: AlertStreamService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.headerTitleService.setTitle('Tableau de bord');
    this.getDepartmentState();
    // Passif : si la session n'est pas admin, `LayoutComponent` n'a jamais ouvert le flux et
    // cet abonnement ne recevra simplement rien — pas de vérification de rôle à dupliquer ici.
    this.alertsSubscription = this.alertStreamService.alerts.subscribe(alert => this.onAlert(alert));
  }

  ngOnDestroy(): void {
    this.alertsSubscription?.unsubscribe();
    // Chart.js garde une référence globale par canvas : sans destruction, revenir sur l'écran
    // laisse l'ancienne instance attachée et le graphe se superpose à lui-même.
    this.charts.forEach(c => c.destroy());
  }

  private onAlert(alert: any): void {
    this.activity.unshift({
      label: alert?.object ?? 'Alerte',
      detail: alert?.address ?? '',
      code: alert?.code ?? 'INFO',
      at: new Date(),
    });
    this.activity.length = Math.min(this.activity.length, DashboardComponent.MAX_ACTIVITY_ITEMS);
    if (this.supervisionStats) {
      // Décompte visible immédiatement, sans attendre le prochain rafraîchissement périodique.
      this.supervisionStats = { ...this.supervisionStats, totalAlerts: this.supervisionStats.totalAlerts + 1 };
    }
  }

  getDepartmentState() {
    this.sharedService.url = '/departmentState';
    this.sharedService.getDepartmentState().subscribe((res: any) => {
      this.departmentState = res;
    });
  }

  ngAfterViewInit() {
    this.http.get<SupervisionStats>(`${environment.apiUrl}${API_PATHS.supervisionStats}`)
      .subscribe({
        next: (stats) => {
          this.supervisionStats = stats;
          this.buildLegends(stats);
          this.renderAlertsChart(stats);

          // Les deux camemberts vivent dans un bloc `@else` conditionné par la longueur de leur
          // légende : au moment où l'on sort de `buildLegends`, Angular n'a pas encore re-rendu,
          // le bloc n'existe pas et les `@ViewChild` sont `undefined` — les graphes restaient
          // donc vides alors que la légende, elle, s'affichait. On force le rendu avant d'aller
          // chercher les canvas.
          this.cdr.detectChanges();
          this.renderDonut(this.alertsByCodeChart, this.codeLegend);
          this.renderDonut(this.typeChart, this.typeLegend);
        },
        error: (err) => console.error('Erreur de chargement des statistiques de supervision', err)
      });
  }

  /** Libellés lisibles des tranches de remplissage renvoyées par le backend. */
  private fillLevelLabel(key: string): string {
    switch (key) {
      case 'NON_INSTRUMENTE': return 'Jamais mesuré';
      case '90-100': return '90-100 % — débordement';
      default: return `${key} %`;
    }
  }

  /** Couleur d'une tranche : vert tant que c'est sain, rouge au débordement, gris si non mesuré. */
  private fillLevelColor(key: string): string {
    if (key === 'NON_INSTRUMENTE') { return '#94A3B8'; }
    if (key.startsWith('90')) { return '#DC2626'; }
    if (key.startsWith('75')) { return '#D97706'; }
    if (key.startsWith('50')) { return '#22C55E'; }
    return '#15803D';
  }

  private codeColor(code: string): string {
    switch (code) {
      case 'DANGER': return '#DC2626';
      case 'WARNING': return '#D97706';
      default: return '#15803D';
    }
  }

  private buildLegends(stats: SupervisionStats): void {
    const fill = stats.depotoirsByFillLevel ?? {};
    this.totalDepotoirs = Object.values(fill).reduce((a, b) => a + b, 0);
    this.fillLevelLegend = Object.keys(fill).map(key => ({
      label: this.fillLevelLabel(key),
      value: fill[key],
      color: this.fillLevelColor(key),
    }));

    this.codeLegend = this.toLegend(stats.alertsByCode ?? {}, code => this.codeColor(code));
    this.typeLegend = this.toLegend(stats.depotoirsByType ?? {});
  }

  private toLegend(source: Record<string, number>, color?: (key: string) => string): LegendEntry[] {
    const keys = Object.keys(source);
    const total = keys.reduce((sum, k) => sum + source[k], 0);
    return keys.map((key, i) => ({
      label: key,
      value: source[key],
      color: color ? color(key) : DashboardComponent.PALETTE[i % DashboardComponent.PALETTE.length],
      percent: total === 0 ? 0 : Math.round((source[key] / total) * 100),
    }));
  }

  private renderAlertsChart(stats: SupervisionStats): void {
    const ctx = this.alertsChart?.nativeElement.getContext('2d');
    if (!ctx) {
      return;
    }
    const chart = new Chart(ctx, {
      type: 'line',
      data: {
        // Les jours sont déjà ordonnés par le backend ; on n'affiche que le jour et le mois,
        // l'année encombrait l'axe pour aucune information.
        labels: stats.alertsPerDay.map(d => d.day.slice(5)),
        datasets: [{
          label: 'Alertes',
          data: stats.alertsPerDay.map(d => d.count),
          borderColor: '#15803D',
          backgroundColor: 'rgba(21, 128, 61, 0.08)',
          fill: true,
          tension: 0.35,
          borderWidth: 2,
          pointRadius: 0,
          pointHoverRadius: 4,
          pointHoverBackgroundColor: '#15803D',
        }],
      },
      options: {
        maintainAspectRatio: false,
        // Légende et titre rendus en HTML autour du graphe (cf. `fillLevelLegend`).
        plugins: { legend: { display: false }, title: { display: false } },
        interaction: { intersect: false, mode: 'index' },
        scales: {
          x: { grid: { display: false }, ticks: { maxRotation: 0, autoSkipPadding: 16, color: '#64748B' } },
          y: { beginAtZero: true, ticks: { precision: 0, color: '#64748B' }, border: { display: false } },
        },
      },
    });
    this.charts.push(chart);
  }

  private renderDonut(ref: ElementRef<HTMLCanvasElement> | undefined, legend: LegendEntry[]): void {
    const ctx = ref?.nativeElement.getContext('2d');
    if (!ctx || legend.length === 0) {
      return;
    }
    const chart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: legend.map(e => e.label),
        datasets: [{
          data: legend.map(e => e.value),
          backgroundColor: legend.map(e => e.color),
          borderWidth: 0,
        }],
      },
      options: {
        maintainAspectRatio: false,
        cutout: '62%',
        plugins: { legend: { display: false }, title: { display: false } },
      },
    });
    this.charts.push(chart);
  }

  /**
   * Décompte d'un statut, 0 s'il est absent.
   *
   * <p>Le typage `Record<string, number>` promet une valeur pour toute clé, ce qui rendait le
   * `?? 0` du template « inutile » aux yeux du compilateur (NG8102) — alors que le backend
   * n'émet que les statuts réellement rencontrés. La garde vit donc ici, où elle est honnête.
   */
  count(source: Record<string, number>, key: string): number {
    return source?.[key] ?? 0;
  }

  /** Classes de la pastille de gravité du fil d'activité. */
  codeBadge(code: string): string {
    switch (code) {
      case 'DANGER': return 'sw-badge sw-badge--danger';
      case 'WARNING': return 'sw-badge sw-badge--warn';
      default: return 'sw-badge sw-badge--ok';
    }
  }
}
