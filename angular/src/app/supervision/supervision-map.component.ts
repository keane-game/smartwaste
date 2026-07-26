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
}

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

    this.depotoirs = depots.map(d => {
      const c = this.centroid(this.toLatLng(d?.coordinates));
      if (!c) { return null; }
      const s = toSvg(c);
      return { x: s.x, y: s.y, address: d?.address || 'Dépotoir', type: d?.typeDepot || '' } as DepotoirMarker;
    }).filter((m): m is DepotoirMarker => m !== null);
  }

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
