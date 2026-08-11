import { LiveAnnouncer } from '@angular/cdk/a11y';
import { SelectionModel } from '@angular/cdk/collections';
import { Component, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { Router } from '@angular/router';
import { SharedService } from '../../../services/shared.service';
import { headerTitleService } from '../../../services/headerTitle.service';
import { MatTableDataSource } from '@angular/material/table';
import { first } from 'rxjs';
import { CreateQuartierComponent } from '../create-quartier/create-quartier.component';
import { ModalService } from '../../../services/modal.service';
import { DeleteComponent } from '../../../shared/components/delete/delete.component';
import { ListUiState } from '../../../shared/ui/list-ui-state';
import { ManagedColumn } from '../../../shared/components/column-manager/column-manager.component';
import { ColumnPreferencesService } from '../../../shared/ui/column-preferences.service';

@Component({
    selector: 'app-quartier',
    templateUrl: './quartier.component.html',
    styleUrl: './quartier.component.scss',
    standalone: false
})
export class QuartierComponent {

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  /**
   * Inventaire des colonnes proposées par « Gérer les colonnes » (maquettes).
   *
   * <p>`select` et `action` sont verrouillées : les masquer priverait l'écran de la sélection
   * et des actions de ligne sans que l'utilisateur comprenne pourquoi.
   */
  readonly manageableColumns: ManagedColumn[] = [
    { key: 'select', label: 'Sélection', locked: true },
    { key: 'quartierName', label: 'Nom' },
    { key: 'quartierCode', label: 'Code' },
    { key: 'quartierLength', label: 'Longueur' },
    { key: 'quartierArea', label: 'Surface' },
    { key: 'quartierZoneCoron', label: 'Statut' },
    { key: 'action', label: 'Actions', locked: true },
  ];

  /** Clé de persistance : le choix de colonnes est propre à l'utilisateur. */
  private static readonly COLUMNS_KEY = 'quartiers.columns';

  // Renseignée dans `ngOnInit` : `columnPrefs` est injecté par le constructeur et n'existe
  // pas encore au moment où les champs s'initialisent.
  displayedColumns: string[] = [];

  /** Menu `…`, sélection et bornes de pagination — voir `ListUiState`. */
  readonly ui = new ListUiState();
  dataSource = new MatTableDataSource<any>([]);
  selection = new SelectionModel<any>(true, []);

  quartiers: any;
  totalPages: number = 1;
  totalQuartiers: number = 0;
  itemsPerPage: number = 10;
  pageSizeOptions: number[] = [5, 10, 20];
  currentPage: number = 0;
  error = '';

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
      QuartierComponent.COLUMNS_KEY, this.manageableColumns, []);
    this.sharedService.url = '/quartiers';
    this.headerTitleService.setTitle('Gestion des quartiers');
    this.loadQuartiers(this.currentPage, this.itemsPerPage);
    //this.openDeleteQuartierModal(-7);
  }

  loadQuartiers(page: number = 0, size: number = 10): void {
    this.sharedService.getResources(page, size).subscribe((resp) => {
      this.dataSource.data = resp.content;
      this.totalQuartiers = resp.totalElements;
      this.dataSource.paginator = this.paginator;
      this.totalPages = Math.ceil(this.totalQuartiers / this.itemsPerPage);
      this.dataSource.sort = this.sort;

      //console.log( this.currentPage);
    });
  }

  onPaginatedChange(event: { pageIndex: number, pageSize: number }) {
    this.currentPage = event.pageIndex;
    this.itemsPerPage = event.pageSize;
    this.loadQuartiers(this.currentPage, this.itemsPerPage);
    console.log(event);
  }

  openCreateQuartierModal() {
    this.modalService.openModal(CreateQuartierComponent, { title: 'Create Quartier' })
      .afterClosed().subscribe(() => this.loadQuartiers(this.currentPage, this.itemsPerPage));
  }

  openUpdateQuartierModal(id: any) {
    const currentQuartier = this.dataSource.data.find((item: any) => item.quartierId === id);
    this.modalService.openModal(CreateQuartierComponent, { id: id, currentQuartier: currentQuartier })
      .afterClosed().subscribe(() => this.loadQuartiers(this.currentPage, this.itemsPerPage));
  }

  openDeleteQuartierModal(id:any) {
    this.modalService.openModal(DeleteComponent, { id: id, url: this.sharedService.url })
      .afterClosed().subscribe(() => this.loadQuartiers(this.currentPage, this.itemsPerPage));
  }

  async closeDialog() {
    try {
      this.modalService.closeAllDialogs(); // make sure it only closes if the upper async fn succesfully ran!
    } catch ($e) {

    }
  }

  applyFilter(event: Event) {
    // console.log((event.target as HTMLInputElement).value)
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
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
    this.columnPrefs.open(QuartierComponent.COLUMNS_KEY, this.manageableColumns, this.displayedColumns)
      .subscribe(columns => { if (columns) { this.displayedColumns = columns; } });
  }
}
