import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EntityConfig, findEntityConfig } from '../entity-config';
import { EntityCrudService } from '../entity-crud.service';

/**
 * Liste générique d'une ressource backend.
 *
 * La ressource est déterminée par la donnée de route `entityKey`, résolue dans `ENTITY_CONFIGS`.
 * Fournit recherche, tri, pagination côté client, suppression et corbeille — fonctions qui
 * manquaient à l'ensemble des écrans « liste » existants.
 *
 * La pagination est appliquée côté client parce que la plupart des endpoints de liste du backend
 * renvoient un tableau complet ; seules quelques ressources exposent un `Page` (chemin sans le
 * « s » final). Basculer sur la pagination serveur ne demandera que d'appeler `page()`.
 */
@Component({
  selector: 'app-entity-list',
  templateUrl: './entity-list.component.html',
  styleUrls: ['./entity-list.component.scss']
})
export class EntityListComponent implements OnInit {

  config!: EntityConfig;
  rows: any[] = [];
  deletions: any[] = [];

  loading = false;
  error = '';
  message = '';
  showTrash = false;

  search = '';
  sortColumn = '';
  sortAsc = true;
  pageIndex = 0;
  pageSize = 10;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private crud: EntityCrudService
  ) { }

  ngOnInit(): void {
    // `data` est un observable : la config doit être relue si l'utilisateur navigue
    // d'une ressource à l'autre sans que le composant soit recréé.
    this.route.data.subscribe(data => {
      const key = data['entityKey'];
      const config = findEntityConfig(key);
      if (!config) {
        this.error = `Ressource inconnue : ${key}`;
        return;
      }
      this.config = config;
      this.resetView();
      this.load();
    });
  }

  private resetView(): void {
    this.rows = [];
    this.deletions = [];
    this.search = '';
    this.sortColumn = '';
    this.pageIndex = 0;
    this.showTrash = false;
    this.message = '';
    this.error = '';
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.crud.list(this.config).subscribe({
      next: rows => { this.rows = rows || []; this.loading = false; },
      error: err => { this.error = err.message; this.loading = false; }
    });
  }

  toggleTrash(): void {
    this.showTrash = !this.showTrash;
    if (this.showTrash) { this.loadDeletions(); }
  }

  loadDeletions(): void {
    this.loading = true;
    this.crud.pendingDeletions(this.config.deletionResource).subscribe({
      next: rows => { this.deletions = rows || []; this.loading = false; },
      error: err => { this.error = err.message; this.loading = false; }
    });
  }

  create(): void {
    this.router.navigate(['create'], { relativeTo: this.route });
  }

  edit(row: any): void {
    this.router.navigate([this.idOf(row), 'edit'], { relativeTo: this.route });
  }

  remove(row: any): void {
    const id = this.idOf(row);
    if (!confirm(`Supprimer ${this.config.label} #${id} ?`)) { return; }
    this.crud.remove(this.config, id).subscribe({
      next: () => {
        this.message = `${this.config.label} supprimé.`;
        this.load();
        if (this.showTrash) { this.loadDeletions(); }
      },
      error: err => { this.error = err.message; }
    });
  }

  restore(row: any): void {
    // La corbeille renvoie une enveloppe { item, purgeDueAt } : l'identifiant est sur `item`.
    const id = this.idOf(row.item ?? row);
    this.crud.restore(this.config.deletionResource, id).subscribe({
      next: () => {
        this.message = `${this.config.label} restauré.`;
        this.loadDeletions();
        this.load();
      },
      error: err => { this.error = err.message; }
    });
  }

  idOf(row: any): any {
    return row ? row[this.config.idField] : undefined;
  }

  sortBy(column: string): void {
    if (this.sortColumn === column) {
      this.sortAsc = !this.sortAsc;
    } else {
      this.sortColumn = column;
      this.sortAsc = true;
    }
  }

  /** Lignes filtrées + triées (avant découpage en pages). */
  get filtered(): any[] {
    const term = this.search.trim().toLowerCase();
    let out = this.rows;
    if (term) {
      out = out.filter(row => this.config.columns
        .some(c => String(row?.[c] ?? '').toLowerCase().includes(term)));
    }
    if (this.sortColumn) {
      const col = this.sortColumn;
      const dir = this.sortAsc ? 1 : -1;
      // Copie avant tri : `sort` mute le tableau, ce qui réordonnerait `rows` en place.
      out = [...out].sort((a, b) => {
        const av = a?.[col], bv = b?.[col];
        if (av == null) { return 1; }
        if (bv == null) { return -1; }
        return typeof av === 'number' && typeof bv === 'number'
          ? (av - bv) * dir
          : String(av).localeCompare(String(bv), 'fr', { numeric: true }) * dir;
      });
    }
    return out;
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filtered.length / this.pageSize));
  }

  get paged(): any[] {
    const start = this.pageIndex * this.pageSize;
    return this.filtered.slice(start, start + this.pageSize);
  }

  goToPage(index: number): void {
    this.pageIndex = Math.min(Math.max(0, index), this.totalPages - 1);
  }

  onSearchChange(): void {
    this.pageIndex = 0;
  }

  labelOf(column: string): string {
    if (column === this.config.idField) { return 'ID'; }
    return this.config.fields.find(f => f.name === column)?.label ?? column;
  }
}
