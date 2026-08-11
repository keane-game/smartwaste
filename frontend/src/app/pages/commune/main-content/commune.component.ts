import { LiveAnnouncer } from '@angular/cdk/a11y';
import { SelectionModel } from '@angular/cdk/collections';
import { Component, EventEmitter, Input, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { SharedService } from '../../../services/shared.service';
import { CreateCommuneComponent } from '../create-commune/create-commune.component';
import { headerTitleService } from '../../../services/headerTitle.service';
import { ModalService } from '../../../services/modal.service';
import { DeleteComponent } from '../../../shared/components/delete/delete.component';
import { ManagedColumn } from '../../../shared/components/column-manager/column-manager.component';
import { ColumnPreferencesService } from '../../../shared/ui/column-preferences.service';

@Component({
    selector: 'app-commune',
    templateUrl: './commune.component.html',
    styleUrl: './commune.component.scss',
    standalone: false
})
export class CommuneComponent {


  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  /**
   * Inventaire des colonnes proposées par « Gérer les colonnes » (maquettes).
   *
   * <p>`select` et `action` sont verrouillées : les masquer priverait l'écran de la sélection et
   * des actions de ligne sans que l'utilisateur comprenne pourquoi.
   */
  readonly manageableColumns: ManagedColumn[] = [
    { key: 'select', label: 'Sélection', locked: true },
    { key: 'communeName', label: 'Nom' },
    { key: 'communeCode', label: 'Code' },
    { key: 'department', label: 'Département' },
    { key: 'totalResident', label: 'Habitants' },
    { key: 'womanResident', label: 'Femmes' },
    { key: 'manResident', label: 'Hommes' },
    { key: 'communeLength', label: 'Longueur' },
    { key: 'communeArea', label: 'Statut' },
    { key: 'action', label: 'Actions', locked: true },
  ];

  /** Clé de persistance : le choix de colonnes est propre à l'utilisateur, pas à la session. */
  private static readonly COLUMNS_KEY = 'communes.columns';

  // Renseignée dans `ngOnInit` et non en initialiseur de champ : `columnPrefs` est injecté par le
  // constructeur et n'existe pas encore au moment où les champs s'initialisent.
  displayedColumns: string[] = [];
  dataSource = new MatTableDataSource<any>([]);
  selection = new SelectionModel<any>(true, []);

  communes: any;
  totalPages: number = 1;
  totalCommunes: number = 0;
  pageSizeOptions: number[] = [5, 10, 20];
  itemsPerPage: number = 10;
  currentPage: number = 0;
  error = '';

  /** Ligne dont le menu `…` est ouvert (`null` = aucun). Patron des maquettes. */
  openRowMenu: string | null = null;

  /**
   * Position à l'écran du menu `…`, en coordonnées de fenêtre.
   *
   * <p>Le menu est positionné en `fixed` et non en `absolute` dans la cellule : le tableau vit
   * dans un conteneur `overflow-x-auto`, et dès qu'un axe n'est pas `visible` l'autre passe à
   * `auto` — le menu en `absolute` était donc rogné par le conteneur et restait invisible bien
   * que présent dans le DOM (vérifié : élément mesuré 176×106 mais jamais affiché).
   */
  rowMenuPos = { top: 0, left: 0 };

  /** Sélection par cases à cocher (colonne de gauche des maquettes). */
  selectedIds = new Set<string>();

  @Input() communeChangeEvent = new EventEmitter<number>();

  constructor(
    private sharedService: SharedService,
    private router: Router,
    private _liveAnnouncer: LiveAnnouncer,
    private headerTitleService: headerTitleService,
    private modalService: ModalService,
    private columnPrefs: ColumnPreferencesService,
  ) { }

  ngOnInit() {
    this.displayedColumns = this.columnPrefs.restore(
      CommuneComponent.COLUMNS_KEY, this.manageableColumns, ['communeLength']);
    this.sharedService.url = '/communes';
    this.loadCommuns(this.currentPage, this.itemsPerPage);
    this.headerTitleService.setTitle('Gestion des communes');
  }

  loadCommuns(page: number = 0, size: number = 10): void {
    this.sharedService.getResources(page, size).subscribe((resp) => {
      this.dataSource.data = resp.content;
      this.totalCommunes = resp.totalElements;
      this.dataSource.paginator = this.paginator;
      this.totalPages = Math.ceil(this.totalCommunes / this.itemsPerPage);
      this.dataSource.sort = this.sort;

    });
  }
  
  onPaginatedChange(event: { pageIndex: number, pageSize: number }) {
    this.currentPage = event.pageIndex;
    this.itemsPerPage = event.pageSize;
    this.loadCommuns(this.currentPage, this.itemsPerPage);
   // console.log(event);
  }

  openCreateCommuneModal() {
    this.modalService.openModal(CreateCommuneComponent, { title: 'Create Commune' })
      .afterClosed().subscribe(() => this.loadCommuns(this.currentPage, this.itemsPerPage));
  }

  openUpdateCommuneModal(id: any) {
    // Deux fautes de frappe corrigees ici : `communId` (le champ reel est `communeId`, la
    // recherche ne trouvait donc jamais rien) et `currentCommun` (`CreateCommuneComponent`
    // attend `currentCommune`) — le formulaire d'edition ne se pre-remplissait jamais.
    const currentCommune = this.dataSource.data.find((item: any) => item.communeId === id);
    this.modalService.openModal(CreateCommuneComponent, { id: id, currentCommune: currentCommune })
      .afterClosed().subscribe(() => this.loadCommuns(this.currentPage, this.itemsPerPage));
  }

  openDeleteCommuneModal(id:any) {
    this.modalService.openModal(DeleteComponent, { id: id, url: this.sharedService.url})
      .afterClosed().subscribe(() => this.loadCommuns(this.currentPage, this.itemsPerPage));
  }
 
  applyFilter(event: Event) {
     const filterValue = (event.target as HTMLInputElement).value;
     this.dataSource.filter = filterValue.trim().toLowerCase();

     if (this.dataSource.paginator) {
       this.dataSource.paginator.firstPage();
     }
   }

  toggleRowMenu(id: string, event: MouseEvent): void {
    if (this.openRowMenu === id) {
      this.openRowMenu = null;
      return;
    }
    const rect = (event.currentTarget as HTMLElement).getBoundingClientRect();
    // Aligné sur le bord droit du bouton (largeur du menu : 176px), juste en dessous.
    this.rowMenuPos = { top: rect.bottom + 4, left: rect.right - 176 };
    this.openRowMenu = id;
  }

  closeRowMenu(): void {
    this.openRowMenu = null;
  }

  toggleSelection(id: string): void {
    if (this.selectedIds.has(id)) {
      this.selectedIds.delete(id);
    } else {
      this.selectedIds.add(id);
    }
  }

  get allSelected(): boolean {
    const rows = this.dataSource.data;
    return rows.length > 0 && rows.every((r: any) => this.selectedIds.has(r.communeId));
  }

  toggleSelectAll(): void {
    if (this.allSelected) {
      this.selectedIds.clear();
    } else {
      this.dataSource.data.forEach((r: any) => this.selectedIds.add(r.communeId));
    }
  }

  /** Bornes affichées par la pagination des maquettes (« 1-10 sur 12 »). */
  get rangeStart(): number {
    return this.totalCommunes === 0 ? 0 : this.currentPage * this.itemsPerPage + 1;
  }

  get rangeEnd(): number {
    return Math.min((this.currentPage + 1) * this.itemsPerPage, this.totalCommunes);
  }

  goToPage(pageIndex: number): void {
    if (pageIndex < 0 || pageIndex >= this.totalPages) {
      return;
    }
    this.onPaginatedChange({ pageIndex, pageSize: this.itemsPerPage });
  }


  openColumnManager(): void {
    this.columnPrefs.open(CommuneComponent.COLUMNS_KEY, this.manageableColumns, this.displayedColumns)
      .subscribe(columns => { if (columns) { this.displayedColumns = columns; } });
  }
 
   /** Announce the change in sort state for assistive technology. */
   announceSortChange(sortState: Sort) {
     if (sortState.direction) {
       this._liveAnnouncer.announce(`Sorted ${sortState.direction}ending`);
     } else {
       this._liveAnnouncer.announce('Sorting cleared');
     }
   }
 
   
   async reload(url: string): Promise<boolean> {
     await this.router.navigateByUrl('/', { skipLocationChange: true });
     return this.router.navigateByUrl(url);
   }
}
