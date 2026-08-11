import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as L from 'leaflet';
import 'esri-leaflet';
import { MapsService, DepotoirMap } from '../../../services/maps.service';

/**
 * Vue "catalogue" des points de collecte (Priorité 5, section territoire/cartographie) : quels
 * points existent et de quel type sont-ils ? Distincte de `DashboardMapComponent`
 * (`pages/dashboard/dashboard-map`) qui répond à "où est le problème, maintenant ?" (remplissage,
 * véhicules, alertes en direct) — voir le commentaire de ce composant pour le choix de ne pas
 * fusionner les deux.
 */
@Component({
  selector: 'app-maps',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './maps.component.html',
  styleUrls: ['./maps.component.scss'],
})
export class MapsComponent implements AfterViewInit, OnDestroy {

  @ViewChild('map', { static: false }) mapElementRef!: ElementRef;
  private map!: L.Map;

  readonly loading = signal(true);
  readonly pointCount = signal(0);

  private readonly departPikineCoords: L.LatLngTuple = [14.7739, -17.3684];

  // Chemins absolus (racine du site) : une URL relative type `../../assets/...` dans une chaine
  // TypeScript n'est jamais reecrite par le compilateur Angular (contrairement a un `src="..."`
  // dans un template) — elle se resout par rapport a l'URL courante du navigateur, pas au fichier
  // source, et cassait silencieusement des lors que la route n'etait pas a la racine.
  private readonly customIconPP = L.icon({ iconUrl: '/assets/images/iconclean.png', iconSize: [28, 28] });
  private readonly customIconBacs = L.icon({ iconUrl: '/assets/images/bacs.png', iconSize: [28, 28] });
  private readonly customIconPrn = L.icon({ iconUrl: '/assets/images/iconprn.png', iconSize: [28, 28] });

  readonly legend = [
    { icon: '/assets/images/bacs.png', label: 'Bac de rue' },
    { icon: '/assets/images/iconclean.png', label: 'Point propre (PP)' },
    { icon: '/assets/images/iconprn.png', label: 'Point de regroupement normalisé (PRN)' },
  ];

  constructor(private mapsService: MapsService) {}

  ngAfterViewInit(): void {
    this.initMap();
    this.addDepartmentPolygon();
    this.addDepotoirMarkers();
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }

  private initMap(): void {
    this.map = L.map(this.mapElementRef.nativeElement, {
      center: this.departPikineCoords,
      zoom: 13,
    });
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
    }).addTo(this.map);
  }

  private addDepartmentPolygon(): void {
    // Les coordonnées `/v1/maps/departments` sont en WGS84 (ADR-0015), en chaînes — `toLatLng()`
    // fait le `parseFloat`, aucune projection à appliquer.
    this.mapsService.getDepartment().subscribe(department => {
      const points = MapsService.toLatLng(department.coordinates);
      if (points.length === 0) {
        return;
      }
      L.polygon([points], { color: '#15803D', weight: 1.5, fillOpacity: 0.03 }).addTo(this.map);
    });
  }

  private addDepotoirMarkers(): void {
    this.mapsService.getDepotoirs().subscribe({
      next: depotoirs => {
        let placed = 0;
        depotoirs.forEach((d: DepotoirMap) => {
          const points = MapsService.toLatLng(d.coordinates);
          if (points.length === 0) {
            return;
          }
          const icon = d.typeDepot === 'PP' ? this.customIconPP
            : d.typeDepot === 'PRN' ? this.customIconPrn
            : this.customIconBacs;
          L.marker(points[0], { icon })
            .addTo(this.map)
            .bindPopup(`${d.address}<br>Type : ${d.typeDepot}`);
          placed++;
        });
        this.pointCount.set(placed);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
