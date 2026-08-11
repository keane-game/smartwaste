import { Component, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatSort, Sort } from '@angular/material/sort';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { SharedService } from '../../../services/shared.service';
import { headerTitleService } from '../../../services/headerTitle.service';
import { ModalService } from '../../../services/modal.service';
import { CreateCircuitBalayageComponent } from '../create-circuit-balayage/create-circuit-balayage.component';
import { DeleteComponent } from '../../../shared/components/delete/delete.component';
import { API_ENDPOINTS } from '../../../shared/constants/api-endpoints';
import { ListUiState } from '../../../shared/ui/list-ui-state';
import { ManagedColumn } from '../../../shared/components/column-manager/column-manager.component';
import { ColumnPreferencesService } from '../../../shared/ui/column-preferences.service';

/**
 * Circuits de balayage. Le composant était entièrement vide (classe sans aucun membre) alors que
 * la route et le module existaient : l'écran s'affichait sans jamais rien charger. Construit ici
 * sur `/v1/circuit-balayages`, dont le backend expose le CRUD complet.
 */
@Component({
    selector: 'app-circuit-balayage',
    templateUrl: './circuit-balayage.component.html',
    styleUrl: './circuit-balayage.component.scss',
    standalone: false
})
export class CircuitBalayageComponent {

  @ViewChild(MatSort) sort!: MatSort;

  /**
   * Inventaire des colonnes proposées par « Gérer les colonnes » (maquettes).
   *
   * <p>`select` et `action` sont verrouillées : les masquer priverait l'écran de la sélection
   * et des actions de ligne sans que l'utilisateur comprenne pourquoi.
   */
  readonly manageableColumns: ManagedColumn[] = [
    { key: 'select', label: 'Sélection', locked: true },
    { key: 'name', label: 'Nom' },
    { key: 'code', label: 'Code' },
    { key: 'shift', label: 'Shift' },
    { key: 'length', label: 'Longueur' },
    { key: 'action', label: 'Actions', locked: true },
  ];

  /** Clé de persistance : le choix de colonnes est propre à l'utilisateur. */
  private static readonly COLUMNS_KEY = 'circuitBalayages.columns';

  // Renseignée dans `ngOnInit` : `columnPrefs` est injecté par le constructeur et n'existe
  // pas encore au moment où les champs s'initialisent.
  displayedColumns: string[] = [];

  /** Menu `…`, sélection et bornes de pagination — voir `ListUiState`. */
  readonly ui = new ListUiState();
  dataSource = new MatTableDataSource<any>([]);

  totalPages: number = 1;
  totalCircuits: number = 0;
  pageSizeOptions: number[] = [5, 10, 20];
  itemsPerPage: number = 10;
  currentPage: number = 0;

  constructor(
    private sharedService: SharedService,
    private _liveAnnouncer: LiveAnnouncer,
    private headerTitleService: headerTitleService,
    private modalService: ModalService,
    private columnPrefs: ColumnPreferencesService,
  ) { }

  ngOnInit() {
    this.displayedColumns = this.columnPrefs.restore(
      CircuitBalayageComponent.COLUMNS_KEY, this.manageableColumns, []);
    this.headerTitleService.setTitle('Gestion des circuits de balayage');
    this.loadCircuits(this.currentPage, this.itemsPerPage);
  }

  loadCircuits(page: number = 0, size: number = 10): void {
    this.sharedService.url = API_ENDPOINTS['circuit-balayages'].basePath;
    this.sharedService.getResources(page, size).subscribe((resp) => {
      this.dataSource.data = resp.content;
      this.totalCircuits = resp.totalElements;
      this.totalPages = Math.ceil(this.totalCircuits / this.itemsPerPage);
      this.dataSource.sort = this.sort;
    });
  }

  onPaginatedChange(event: { pageIndex: number, pageSize: number }) {
    this.currentPage = event.pageIndex;
    this.itemsPerPage = event.pageSize;
    this.loadCircuits(this.currentPage, this.itemsPerPage);
  }

  openCreateModal() {
    this.modalService.openModal(CreateCircuitBalayageComponent, { title: 'Create CircuitBalayage' })
      .afterClosed().subscribe(() => this.loadCircuits(this.currentPage, this.itemsPerPage));
  }

  openUpdateModal(id: any) {
    const current = this.dataSource.data.find((item: any) => item.circuitbalayageId === id);
    this.modalService.openModal(CreateCircuitBalayageComponent, { id: id, currentCircuitBalayage: current })
      .afterClosed().subscribe(() => this.loadCircuits(this.currentPage, this.itemsPerPage));
  }

  openDeleteModal(id: any) {
    this.modalService.openModal(DeleteComponent, { id: id, url: API_ENDPOINTS['circuit-balayages'].basePath })
      .afterClosed().subscribe(() => this.loadCircuits(this.currentPage, this.itemsPerPage));
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  /** Announce the change in sort state for assistive technology. */
  announceSortChange(sortState: Sort) {
    if (sortState.direction) {
      this._liveAnnouncer.announce(`Sorted ${sortState.direction}ending`);
    } else {
      this._liveAnnouncer.announce('Sorting cleared');
    }
  }

  goToPage(pageIndex: number): void {
    if (pageIndex < 0 || pageIndex >= this.totalPages) { return; }
    this.onPaginatedChange({ pageIndex, pageSize: this.itemsPerPage });
  }

  openColumnManager(): void {
    this.columnPrefs.open(CircuitBalayageComponent.COLUMNS_KEY, this.manageableColumns, this.displayedColumns)
      .subscribe(columns => { if (columns) { this.displayedColumns = columns; } });
  }
}
