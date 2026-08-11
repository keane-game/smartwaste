import { Component, EventEmitter, Input, ViewChild } from '@angular/core';
import { headerTitleService } from '../../../services/headerTitle.service';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { SelectionModel } from '@angular/cdk/collections';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { SharedService } from '../../../services/shared.service';
import { CreateDepotoirComponent } from '../create-depotoir/create-depotoir.component';
import { ModalService } from '../../../services/modal.service';
import { DeleteComponent } from '../../../shared/components/delete/delete.component';
import { ListUiState } from '../../../shared/ui/list-ui-state';
import { ManagedColumn } from '../../../shared/components/column-manager/column-manager.component';
import { ColumnPreferencesService } from '../../../shared/ui/column-preferences.service';

@Component({
    selector: 'app-depotoir',
    templateUrl: './depotoir.component.html',
    styleUrls: ['./depotoir.component.scss'],
    standalone: false
})
export class DepotoirComponent {


  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  /** Menu `…`, sélection et bornes de pagination — voir `ListUiState`. */
  readonly ui = new ListUiState();

  /**
   * Inventaire des colonnes proposées par « Gérer les colonnes » (maquettes).
   *
   * <p>`select` et `action` sont verrouillées : les masquer priverait l'écran de la sélection et
   * des actions de ligne.
   */
  readonly manageableColumns: ManagedColumn[] = [
    { key: 'select', label: 'Sélection', locked: true },
    { key: 'depotoirAddress', label: 'Adresse' },
    { key: 'typeDepotoir', label: 'Type' },
    { key: 'commune', label: 'Commune' },
    { key: 'longitude', label: 'Longitude' },
    { key: 'latitude', label: 'Latitude' },
    { key: 'action', label: 'Actions', locked: true },
  ];

  /** Clé de persistance : le choix de colonnes est propre à l'utilisateur. */
  private static readonly COLUMNS_KEY = 'depotoirs.columns';

  // Renseignée dans `ngOnInit` : `columnPrefs` est injecté par le constructeur.
  displayedColumns: string[] = [];
  dataSource = new MatTableDataSource<any>([]);
  selection = new SelectionModel<any>(true, []);

  depotoirs: any;
  totalPages: number = 1;
  totalDepotoirs: number = 0;
  pageSizeOptions: number[] = [5, 10, 20];
  itemsPerPage: number = 10;
  currentPage: number = 0;
  error = '';

  @Input() depotChangeEvent = new EventEmitter<number>();

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
      DepotoirComponent.COLUMNS_KEY, this.manageableColumns, ['longitude', 'latitude']);
    this.sharedService.url = '/depotoirs';
    this.loadDepotoirs();
    this.headerTitleService.setTitle('Gestion des dépotoirs');
  }


  loadDepotoirs(page: number = 0, size: number = 10): void {
    this.sharedService.getResources(page, size).subscribe((resp) => {
      this.dataSource.data = resp.content;
      this.totalDepotoirs = resp.totalElements;
      this.dataSource.paginator = this.paginator;
      this.totalPages = Math.ceil(this.totalDepotoirs / this.itemsPerPage);
      this.dataSource.sort = this.sort;

      console.log( resp.content);
    });
  }

  onPaginatedChange(event: { pageIndex: number, pageSize: number }) {
    this.currentPage = event.pageIndex;
    this.itemsPerPage = event.pageSize;
    this.loadDepotoirs(this.currentPage, this.itemsPerPage);
    console.log(event);
  }

  openCreateDepotoirModal() {
    this.modalService.openModal(CreateDepotoirComponent, { title: 'Create Depotoir' })
      .afterClosed().subscribe(() => this.loadDepotoirs(this.currentPage, this.itemsPerPage));
  }

  openUpdateDepotoirModal(id: any) {
    const currentDepotoir = this.dataSource.data.find((item: any) => item.depotoirId === id);
    //console.log(id)
    this.modalService.openModal(CreateDepotoirComponent, { id: id, currentDepotoir: currentDepotoir })
      .afterClosed().subscribe(() => this.loadDepotoirs(this.currentPage, this.itemsPerPage));
  }

  openDeleteDepotoirModal(id:any) {
    this.modalService.openModal(DeleteComponent, { id: id, url: this.sharedService.url })
      .afterClosed().subscribe(() => this.loadDepotoirs(this.currentPage, this.itemsPerPage));
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
 
 
 
   async reload(url: string): Promise<boolean> {
     await this.router.navigateByUrl('/', { skipLocationChange: true });
     return this.router.navigateByUrl(url);
   }

  

  goToPage(pageIndex: number): void {
    if (pageIndex < 0 || pageIndex >= this.totalPages) { return; }
    this.onPaginatedChange({ pageIndex, pageSize: this.itemsPerPage });
  }

  openColumnManager(): void {
    this.columnPrefs.open(DepotoirComponent.COLUMNS_KEY, this.manageableColumns, this.displayedColumns)
      .subscribe(columns => { if (columns) { this.displayedColumns = columns; } });
  }
}
