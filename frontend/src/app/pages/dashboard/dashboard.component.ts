import { Component, ElementRef, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { headerTitleService } from '../../services/headerTitle.service';
import  { Chart } from 'chart.js/auto';
import { imgDashboardConstant } from '../../shared/constants/images.constant';
import { SharedService } from '../../services/shared.service';
import { API_PATHS } from '../../shared/constants/api-endpoints';
import { environment } from '../../../environments/environment';

/** Sous-ensemble de `SupervisionStats` (backend) utilisé par ce tableau de bord. */
interface SupervisionStats {
  alertsPerDay: { day: string; count: number }[];
  alertsByCode: Record<string, number>;
  totalAlerts: number;
  alertsLast7Days: number;
  liveSubscribers: number;
  ingestion: {
    silentSensors: unknown[];
  };
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent {

  departmentState: any;
  supervisionStats: SupervisionStats | null = null;
  imgConstants = imgDashboardConstant;
  @ViewChild('mychart') mychart!: ElementRef<HTMLCanvasElement>;
  @ViewChild('alertsByCodeChart') alertsByCodeChart!: ElementRef<HTMLCanvasElement>;

  constructor(
    private headerTitleService: headerTitleService,
    private sharedService: SharedService,
    private http: HttpClient,
  ){}

  ngOnInit() {
    this.headerTitleService.setTitle('Dashboard');
    this.getDepartmentState();
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
          backgroundColor: ['#4154f1', '#ff771d', '#dc3545', '#2eca6a'],
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

}
