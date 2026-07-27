import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { forkJoin } from 'rxjs';
import { MapsService } from '../services/maps.service';
import { headerTitleService } from '../services/headerTitle.service';

interface Pt { x: number; y: number; }
interface DepotoirMarker {
  x: number; y: number;
  address: string;
  type: string;
  color: string;
}
interface TypeStat { name: string; count: number; color: string; }

/**
 * Carte de supervision — rendu SVG autonome (sans dépendance cartographique externe).
 *
 * Consomme `/v1/maps/departments` (contour) et `/v1/maps/depotoirs` (points) et projette les
 * latitudes/longitudes réelles dans un `viewBox` normalisé : contour du département en polygone,
 * dépotoirs en marqueurs cliquables. Remplace l'absence totale d'exploitation de ces endpoints.
 */
@Component({
  selector: 'app-supervision-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './supervision-map.component.html',
  styleUrls: ['./supervision-map.component.scss']
})
export class SupervisionMapComponent implements OnInit {

  readonly W = 900;
  readonly H = 640;
  private readonly pad = 30;

  loading = false;
  error = '';

  departmentName = '';
  departmentPolygon = '';                 // attribut `points` du <polygon>
  depotoirs: DepotoirMarker[] = [];
  selected: DepotoirMarker | null = null;

  /** Répartition des dépotoirs par type (pour la légende/filtre). */
  typeStats: TypeStat[] = [];
  /** Types actuellement affichés ; vide au départ = tous visibles. */
  private hidden = new Set<string>();

  /** Palette stable par type, assignée dans l'ordre d'apparition. */
  private readonly palette = ['#dc2626', '#16a34a', '#2563eb', '#d97706', '#7c3aed', '#0891b2', '#db2777', '#65a30d'];
  private readonly UNTYPED = 'Non typé';

  constructor(
    private mapsService: MapsService,
    private headerTitleService: headerTitleService
  ) { }

  ngOnInit(): void {
    this.headerTitleService.setTitle('Carte de supervision');
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    forkJoin({
      dept: this.mapsService.getDepartment(),
      depots: this.mapsService.getDepotoirs()
    }).subscribe({
      next: ({ dept, depots }) => { this.project(dept, depots || []); this.loading = false; },
      error: () => { this.loading = false; this.error = 'Impossible de charger les données cartographiques.'; }
    });
  }

  /** Convertit les coordonnées géographiques en repère SVG (bornes calculées sur l'ensemble). */
  private project(dept: any, depots: any[]): void {
    const deptCoords = this.toLatLng(dept?.coordinates);
    const depotCentroids = depots.map(d => this.centroid(this.toLatLng(d?.coordinates)))
      .filter((p): p is Pt => p !== null);

    const all: Pt[] = [...deptCoords, ...depotCentroids];
    if (!all.length) { this.error = 'Aucune coordonnée disponible.'; return; }

    const lats = all.map(p => p.y), lngs = all.map(p => p.x);
    const minLat = Math.min(...lats), maxLat = Math.max(...lats);
    const minLng = Math.min(...lngs), maxLng = Math.max(...lngs);
    const spanLat = (maxLat - minLat) || 1e-6;
    const spanLng = (maxLng - minLng) || 1e-6;

    const toSvg = (p: Pt): Pt => ({
      // longitude → x ; latitude → y (axe SVG inversé : nord en haut)
      x: this.pad + ((p.x - minLng) / spanLng) * (this.W - 2 * this.pad),
      y: this.pad + (1 - (p.y - minLat) / spanLat) * (this.H - 2 * this.pad)
    });

    this.departmentName = dept?.name || 'Département';
    this.departmentPolygon = deptCoords.map(toSvg).map(p => `${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ');

    // Couleur stable par type (dans l'ordre d'apparition).
    const colorByType = new Map<string, string>();
    const colorFor = (type: string): string => {
      if (!colorByType.has(type)) {
        colorByType.set(type, this.palette[colorByType.size % this.palette.length]);
      }
      return colorByType.get(type)!;
    };

    this.depotoirs = depots.map(d => {
      const c = this.centroid(this.toLatLng(d?.coordinates));
      if (!c) { return null; }
      const s = toSvg(c);
      const type = (d?.typeDepot || '').trim() || this.UNTYPED;
      return { x: s.x, y: s.y, address: d?.address || 'Dépotoir', type, color: colorFor(type) } as DepotoirMarker;
    }).filter((m): m is DepotoirMarker => m !== null);

    // Statistiques par type pour la légende.
    const counts = new Map<string, number>();
    this.depotoirs.forEach(m => counts.set(m.type, (counts.get(m.type) || 0) + 1));
    this.typeStats = [...counts.entries()]
      .map(([name, count]) => ({ name, count, color: colorFor(name) }))
      .sort((a, b) => b.count - a.count);
  }

  /** Dépotoirs visibles selon les types actifs. */
  get visibleDepotoirs(): DepotoirMarker[] {
    if (!this.hidden.size) { return this.depotoirs; }
    return this.depotoirs.filter(m => !this.hidden.has(m.type));
  }

  /** Active/désactive l'affichage d'un type. */
  toggleType(name: string): void {
    if (this.hidden.has(name)) { this.hidden.delete(name); }
    else {
      this.hidden.add(name);
      if (this.selected && this.selected.type === name) { this.selected = null; }
    }
  }

  isHidden(name: string): boolean { return this.hidden.has(name); }

  /** Parse une liste de Coordinate (latitude/longitude en chaînes) en points {x:lng, y:lat}. */
  private toLatLng(coords: any[]): Pt[] {
    if (!Array.isArray(coords)) { return []; }
    return coords
      .map(c => ({ x: parseFloat(c?.longitude), y: parseFloat(c?.latitude) }))
      .filter(p => !isNaN(p.x) && !isNaN(p.y));
  }

  private centroid(pts: Pt[]): Pt | null {
    if (!pts.length) { return null; }
    const sum = pts.reduce((a, p) => ({ x: a.x + p.x, y: a.y + p.y }), { x: 0, y: 0 });
    return { x: sum.x / pts.length, y: sum.y / pts.length };
  }

  select(m: DepotoirMarker): void { this.selected = m; }
}
