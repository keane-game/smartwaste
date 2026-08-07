import { Component, ElementRef, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { headerTitleService } from '../../services/headerTitle.service';
import  { Chart } from 'chart.js/auto';
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

@Component({
  selector: 'app-dashboard',
  standalone: true,
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
  @ViewChild('mychart') mychart!: ElementRef<HTMLCanvasElement>;
  @ViewChild('alertsByCodeChart') alertsByCodeChart!: ElementRef<HTMLCanvasElement>;
  @ViewChild('fillLevelChart') fillLevelChart!: ElementRef<HTMLCanvasElement>;

  private alertsSubscription?: Subscription;

  constructor(
    private headerTitleService: headerTitleService,
    private sharedService: SharedService,
    private http: HttpClient,
    private alertStreamService: AlertStreamService,
  ){}

  ngOnInit() {
    this.headerTitleService.setTitle('Tableau de bord');
    this.getDepartmentState();
    // Passif : si la session n'est pas admin, `LayoutComponent` n'a jamais ouvert le flux et
    // cet abonnement ne recevra simplement rien — pas de vérification de rôle à dupliquer ici.
    this.alertsSubscription = this.alertStreamService.alerts.subscribe(alert => this.onAlert(alert));
  }

  ngOnDestroy(): void {
    this.alertsSubscription?.unsubscribe();
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

  getDepartmentState(){
    this.sharedService.url = "/departmentState"
    this.sharedService.getDepartmentState().subscribe((res: any) => {
      this.departmentState = res;
    });
  }

  ngAfterViewInit(){
    this.http.get<SupervisionStats>(`${environment.apiUrl}${API_PATHS.supervisionStats}`)
      .subscribe({
        next: (stats) => {
          this.supervisionStats = stats;
          this.renderAlertsChart(stats);
          this.renderAlertsByCodeChart(stats);
          this.renderFillLevelChart(stats);
        },
        error: (err) => console.error('Erreur de chargement des statistiques de supervision', err)
      });
  }

  private renderAlertsChart(stats: SupervisionStats): void {
    const ctx = this.mychart.nativeElement.getContext('2d');
    if (!ctx) {
      return;
    }
    new Chart(ctx, {
      type: 'line',
      data: {
        labels: stats.alertsPerDay.map(d => d.day),
        datasets: [{
          label: 'Alertes par jour',
          data: stats.alertsPerDay.map(d => d.count),
          tension: 0.3,
          pointStyle: false,
          borderWidth: 1.5,
        }],
      },
      options: {
        maintainAspectRatio: false,
        plugins: {
          title: {
            display: true,
            text: 'Alertes',
            color: 'black',
          },
          legend: {
            position: 'bottom',
            labels: {
              boxWidth: 30,
              boxHeight: 1,
              font: { size: 9 },
            }
          }
        }
      }
    });
  }

  private renderAlertsByCodeChart(stats: SupervisionStats): void {
    const ctx = this.alertsByCodeChart.nativeElement.getContext('2d');
    if (!ctx) {
      return;
    }
    const codes = Object.keys(stats.alertsByCode);
    new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: codes,
        datasets: [{
          data: codes.map(code => stats.alertsByCode[code]),
          backgroundColor: ['#15803D', '#A16207', '#DC2626', '#22C55E'],
        }],
      },
      options: {
        maintainAspectRatio: false,
        plugins: {
          title: {
            display: true,
            text: 'Alertes par code',
            color: 'black',
          },
          legend: {
            position: 'bottom',
          }
        }
      }
    });
  }

  private renderFillLevelChart(stats: SupervisionStats): void {
    const ctx = this.fillLevelChart.nativeElement.getContext('2d');
    if (!ctx) {
      return;
    }
    const buckets = Object.keys(stats.depotoirsByFillLevel);
    new Chart(ctx, {
      type: 'bar',
      data: {
        labels: buckets,
        datasets: [{
          label: 'Points de collecte',
          data: buckets.map(bucket => stats.depotoirsByFillLevel[bucket]),
          backgroundColor: '#15803D',
        }],
      },
      options: {
        maintainAspectRatio: false,
        scales: {
          y: { beginAtZero: true, ticks: { precision: 0 } }
        },
        plugins: {
          title: {
            display: true,
            text: 'Bacs par tranche de remplissage',
            color: 'black',
          },
          legend: { display: false }
        }
      }
    });
  }

}
