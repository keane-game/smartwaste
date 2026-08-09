import { Component, ElementRef, OnDestroy, AfterViewInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as L from 'leaflet';
import { Subscription, interval } from 'rxjs';
import { startWith, switchMap } from 'rxjs/operators';
import { MapsService, DepotoirMap, VehicleOnMap, fillLevelBucket, FILL_LEVEL_COLORS } from '../../../services/maps.service';
import { AlertStreamService } from '../../../services/alert-stream.service';

/**
 * Carte vivante du tableau de bord (Priorité 1 / 4 du brief produit).
 *
 * <p>Distincte de `MapsComponent` (`pages/maps`) qui répond à une question différente — « quels
 * sont les points et leur type ? ». Celle-ci répond à « où est le problème, maintenant ? » :
 * remplissage des bacs (couleur), véhicules en circulation (rafraîchis, pas de flux dédié côté
 * backend), et alertes poussées en direct par SSE. Dupliquer l'init Leaflet plutôt que fusionner
 * les deux évite de faire porter à un composant partagé deux intentions différentes.
 */
@Component({
    selector: 'app-dashboard-map',
    imports: [CommonModule],
    templateUrl: './dashboard-map.component.html',
    styleUrls: ['./dashboard-map.component.scss']
})
export class DashboardMapComponent implements AfterViewInit, OnDestroy {

  @ViewChild('dashboardMap', { static: false }) mapElementRef!: ElementRef;
  private map!: L.Map;
  private vehicleMarkers = new Map<string, L.Marker>();
  private alertMarkers: L.Marker[] = [];
  private vehiclesSubscription?: Subscription;
  private alertsSubscription?: Subscription;

  readonly departPikineCoords: L.LatLngTuple = [14.7739, -17.3684];
  readonly legend = [
    { bucket: 'ok', label: '< 50 %', color: FILL_LEVEL_COLORS['ok'] },
    { bucket: 'warning', label: '50–80 %', color: FILL_LEVEL_COLORS['warning'] },
    { bucket: 'danger', label: '≥ 80 %', color: FILL_LEVEL_COLORS['danger'] },
    { bucket: 'unknown', label: 'Jamais mesuré', color: FILL_LEVEL_COLORS['unknown'] },
  ];

  constructor(
    private mapsService: MapsService,
    private alertStreamService: AlertStreamService,
  ) { }

  ngAfterViewInit(): void {
    this.initMap();
    this.addDepartmentPolygon();
    this.addDepotoirMarkers();
    this.watchVehicles();
    this.watchAlerts();
  }

  ngOnDestroy(): void {
    this.vehiclesSubscription?.unsubscribe();
    this.alertsSubscription?.unsubscribe();
    this.map?.remove();
  }

  private initMap(): void {
    this.map = L.map(this.mapElementRef.nativeElement, {
      center: this.departPikineCoords,
      zoom: 12,
    });
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
    }).addTo(this.map);
  }

  private addDepartmentPolygon(): void {
    this.mapsService.getDepartment().subscribe(department => {
      const points = MapsService.toLatLng(department.coordinates);
      if (points.length === 0) {
        return;
      }
      // #15803D = --color-primary (assets/scss/variables.scss). Un style Leaflet inline ne peut
      // pas lire une variable CSS, d'où la valeur en dur plutôt qu'un `var(...)`.
      L.polygon([points], { color: '#15803D', weight: 1, fillOpacity: 0.03 }).addTo(this.map);
    });
  }

  private addDepotoirMarkers(): void {
    this.mapsService.getDepotoirs().subscribe(depotoirs => {
      depotoirs.forEach((d: DepotoirMap) => {
        const points = MapsService.toLatLng(d.coordinates);
        if (points.length === 0) {
          return;
        }
        const bucket = fillLevelBucket(d.fillLevelPercent);
        const level = d.fillLevelPercent === null || d.fillLevelPercent === undefined
          ? 'jamais mesuré' : `${d.fillLevelPercent} %`;
        L.circleMarker(points[0], {
          radius: 7,
          color: '#fff',
          weight: 1,
          fillColor: FILL_LEVEL_COLORS[bucket],
          fillOpacity: 0.9,
        })
          .addTo(this.map)
          .bindPopup(`${d.address}<br>Remplissage : ${level}`);
      });
    });
  }

  /** Pas de flux SSE côté backend pour les véhicules (audit 2026-08) : un sondage périodique
   *  reste la seule option honnête tant qu'il n'existe pas. */
  private watchVehicles(): void {
    this.vehiclesSubscription = interval(30000).pipe(
      startWith(0),
      switchMap(() => this.mapsService.getVehicles()),
    ).subscribe(vehicles => this.renderVehicles(vehicles));
  }

  private renderVehicles(vehicles: VehicleOnMap[]): void {
    const seen = new Set<string>();
    vehicles.forEach(v => {
      seen.add(v.vehicleId);
      const position: L.LatLngTuple = [v.latitude, v.longitude];
      const existing = this.vehicleMarkers.get(v.vehicleId);
      if (existing) {
        existing.setLatLng(position);
      } else {
        const marker = L.marker(position, {
          icon: L.divIcon({ className: 'dashboard-vehicle-icon', html: '🚛', iconSize: [24, 24] }),
        }).addTo(this.map).bindPopup(`${v.label} (${v.registration})`);
        this.vehicleMarkers.set(v.vehicleId, marker);
      }
    });
    // Un véhicule qui sort de la fenêtre de fraîcheur du backend disparaît de la réponse :
    // on retire son marqueur plutôt que de laisser une position figée mentir sur la carte.
    for (const [id, marker] of this.vehicleMarkers) {
      if (!seen.has(id)) {
        marker.remove();
        this.vehicleMarkers.delete(id);
      }
    }
  }

  private watchAlerts(): void {
    this.alertsSubscription = this.alertStreamService.alerts.subscribe(alert => {
      const points = MapsService.toLatLng(alert?.coordinate ? [alert.coordinate] : []);
      if (points.length === 0) {
        return; // alerte sans localisation (saisie manuelle sans géométrie) : rien à poser
      }
      const marker = L.marker(points[0], {
        icon: L.divIcon({ className: 'dashboard-alert-icon', html: '🚨', iconSize: [26, 26] }),
      }).addTo(this.map).bindPopup(`${alert?.object ?? 'Alerte'}<br>${alert?.address ?? ''}`).openPopup();
      this.alertMarkers.push(marker);
      this.map.panTo(points[0]);
      // Une alerte est un événement, pas un état durable affiché indéfiniment sur la carte :
      // elle s'efface après deux minutes, le temps d'être remarquée.
      setTimeout(() => {
        marker.remove();
        this.alertMarkers = this.alertMarkers.filter(m => m !== marker);
      }, 120000);
    });
  }
}
