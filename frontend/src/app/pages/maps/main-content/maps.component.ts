import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import * as L from 'leaflet';
import 'esri-leaflet';
import { MapsService, DepotoirMap, fillLevelBucket, FILL_LEVEL_COLORS } from '../../../services/maps.service';
import { headerTitleService } from '../../../services/headerTitle.service';

/** Un point de collecte tel que l'écran le manipule : le read-model plus sa position résolue. */
interface MapPoint {
  /** Identifiant stable pour la sélection — l'API ne renvoie pas d'id sur ce read-model. */
  id: string;
  depotoir: DepotoirMap;
  position: L.LatLngTuple;
  marker: L.Marker;
}

/**
 * Carte des points de collecte, d'après les maquettes de référence (frontend/mockup, « Carte ») :
 * panneau liste cherchable à gauche, carte à droite, élément sélectionné mis en évidence des deux
 * côtés, et fiche de détail au clic.
 *
 * <p>Distincte de `DashboardMapComponent` qui répond à « où est le problème, maintenant ? »
 * (remplissage, véhicules, alertes en direct). Celle-ci répond à « quels points existent, et
 * lequel je cherche ? ».
 */
@Component({
  selector: 'app-maps',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './maps.component.html',
  styleUrls: ['./maps.component.scss'],
})
export class MapsComponent implements AfterViewInit, OnDestroy {

  @ViewChild('map', { static: false }) mapElementRef!: ElementRef;
  private map!: L.Map;

  readonly loading = signal(true);
  readonly points = signal<MapPoint[]>([]);
  readonly search = signal('');
  readonly selectedId = signal<string | null>(null);

  /** Liste filtrée par la recherche — même source que les marqueurs, jamais désynchronisée. */
  readonly filtered = computed(() => {
    const term = this.search().trim().toLowerCase();
    const all = this.points();
    if (!term) { return all; }
    return all.filter(p =>
      (p.depotoir.address ?? '').toLowerCase().includes(term)
      || (p.depotoir.typeDepot ?? '').toLowerCase().includes(term));
  });

  readonly selected = computed(() =>
    this.points().find(p => p.id === this.selectedId()) ?? null);

  private readonly departPikineCoords: L.LatLngTuple = [14.7739, -17.3684];

  // Chemins absolus : une URL relative dans une chaîne TypeScript n'est jamais réécrite par le
  // compilateur Angular (contrairement à un `src="…"` de template) — elle se résout par rapport à
  // l'URL courante et cassait dès que la route n'était pas à la racine.
  private readonly icons: Record<string, L.Icon> = {
    PP: L.icon({ iconUrl: '/assets/images/iconclean.png', iconSize: [28, 28] }),
    PRN: L.icon({ iconUrl: '/assets/images/iconprn.png', iconSize: [28, 28] }),
    DEFAULT: L.icon({ iconUrl: '/assets/images/bacs.png', iconSize: [28, 28] }),
  };

  readonly legend = [
    { icon: '/assets/images/bacs.png', label: 'Bac de rue' },
    { icon: '/assets/images/iconclean.png', label: 'Point propre (PP)' },
    { icon: '/assets/images/iconprn.png', label: 'Point de regroupement normalisé (PRN)' },
  ];

  constructor(
    private mapsService: MapsService,
    private headerTitleService: headerTitleService,
  ) {
    // Sans cela l'en-tete gardait « Dashboard », le titre par defaut de `HeaderComponent`.
    this.headerTitleService.setTitle('Carte');
  }

  ngAfterViewInit(): void {
    this.initMap();
    this.addDepartmentPolygon();
    this.loadPoints();
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
    // Coordonnées `/v1/maps/departments` en WGS84 (ADR-0015), sérialisées en chaînes — `toLatLng()`
    // fait le `parseFloat`, aucune projection à appliquer.
    this.mapsService.getDepartment().subscribe(department => {
      const pts = MapsService.toLatLng(department.coordinates);
      if (pts.length === 0) { return; }
      L.polygon([pts], { color: '#15803D', weight: 1.5, fillOpacity: 0.03 }).addTo(this.map);
    });
  }

  private loadPoints(): void {
    this.mapsService.getDepotoirs().subscribe({
      next: depotoirs => {
        const built: MapPoint[] = [];
        depotoirs.forEach((d, index) => {
          const coords = MapsService.toLatLng(d.coordinates);
          if (coords.length === 0) { return; }
          const icon = this.icons[d.typeDepot] ?? this.icons['DEFAULT'];
          const marker = L.marker(coords[0], { icon }).addTo(this.map);
          // L'API ne fournit pas d'identifiant sur ce read-model : on en dérive un stable à partir
          // du rang, suffisant pour lier une ligne de liste à son marqueur le temps de la session.
          const id = `p${index}`;
          marker.on('click', () => this.select(id));
          built.push({ id, depotoir: d, position: coords[0], marker });
        });
        this.points.set(built);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  /** Sélectionne un point : centre la carte et met la ligne en évidence. */
  select(id: string): void {
    this.selectedId.set(id);
    const point = this.points().find(p => p.id === id);
    if (point) {
      this.map.panTo(point.position);
    }
  }

  clearSelection(): void {
    this.selectedId.set(null);
  }

  onSearch(value: string): void {
    this.search.set(value);
  }

  /** Libellé du remplissage : « jamais mesuré » est une information, pas une absence. */
  fillLabel(d: DepotoirMap): string {
    return d.fillLevelPercent === null || d.fillLevelPercent === undefined
      ? 'Jamais mesuré'
      : `${d.fillLevelPercent} %`;
  }

  fillColor(d: DepotoirMap): string {
    return FILL_LEVEL_COLORS[fillLevelBucket(d.fillLevelPercent)];
  }
}
