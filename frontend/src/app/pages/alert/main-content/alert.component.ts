import { Component, ViewChild } from '@angular/core';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { SharedService } from '../../../services/shared.service';
import { headerTitleService } from '../../../services/headerTitle.service';
import { ModalService } from '../../../services/modal.service';
import { CreateAlertComponent } from '../create-alert/create-alert.component';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { DeleteComponent } from '../../../shared/components/delete/delete.component';
import { API_ENDPOINTS } from '../../../shared/constants/api-endpoints';

@Component({
    selector: 'app-alert',
    templateUrl: './alert.component.html',
    styleUrl: './alert.component.scss',
    standalone: false
})
export class AlertComponent {

  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = ['picture', 'object', 'message', 'code', 'adress', 'action'];
  dataSource = new MatTableDataSource<any>([]);

  totalPages: number = 1;
  totalAlerts: number = 0;
  pageSizeOptions: number[] = [5, 10, 20];
  itemsPerPage: number = 10;
  currentPage: number = 0;

  /** Agrandissement au survol d'une vignette : `null` = aucune. */
  zoomedImage: string | null = null;

  constructor (
    private sharedService: SharedService,
    private headerTitleServie: headerTitleService,
    private modalServie: ModalService,
    private _liveAnnouncer: LiveAnnouncer,
  ) {}

  ngOnInit() {
    this.sharedService.url = API_ENDPOINTS.alerts.basePath;
    this.loadAlerts(this.currentPage, this.itemsPerPage);
    this.headerTitleServie.setTitle('Gestion des alertes');
  }

  loadAlerts(page: number, size: number) {
    this.sharedService.getResources(page, size).subscribe((resp) => {
      this.dataSource.data = resp.content;
      this.totalAlerts = resp.totalElements;
      this.totalPages = Math.ceil(this.totalAlerts / this.itemsPerPage);
      this.dataSource.sort = this.sort;
    });
  }

  onPaginatedChange(event: { pageIndex: number, pageSize: number }) {
    this.currentPage = event.pageIndex;
    this.itemsPerPage = event.pageSize;
    this.loadAlerts(this.currentPage, this.itemsPerPage);
  }

  openCreateAlertModal() {
    this.modalServie.openModal(CreateAlertComponent)
      .afterClosed().subscribe(() => this.loadAlerts(this.currentPage, this.itemsPerPage));
  }

  openUpdateAlertModal(id: any) {
    const currentAlert = this.dataSource.data.find((item: any) => item.alertId === id);
    this.modalServie.openModal(CreateAlertComponent, { id: id, currentAlert: currentAlert })
      .afterClosed().subscribe(() => this.loadAlerts(this.currentPage, this.itemsPerPage));
  }

  openDeleteAlertModal(id: any) {
    this.modalServie.openModal(DeleteComponent, { id: id, url: API_ENDPOINTS.alerts.basePath })
      .afterClosed().subscribe(() => this.loadAlerts(this.currentPage, this.itemsPerPage));
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  /** Classes du badge de sévérité — DANGER/WARNING/INFO sont les trois seules valeurs d'`AlertCode`. */
  codeClasses(code: string): string {
    switch (code) {
      case 'DANGER': return 'bg-destructive/10 text-destructive';
      case 'WARNING': return 'bg-warning/10 text-warning';
      default: return 'bg-primary/10 text-primary';
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

  // Remplace `onMouseMove`/`onMouseLeave`, qui repositionnaient la vignette en absolute avec un
  // `scale(8)` et des pourcentages en dur : l'image agrandie sortait de la ligne, se faisait
  // rogner par le tableau et laissait un trou dans la cellule. Une lightbox au clic est
  // previsible et n'affecte pas la mise en page du tableau.
  openZoom(element: any): void {
    if (element?.image?.data) {
      this.zoomedImage = 'data:image/png;base64,' + element.image.data;
    }
  }

  closeZoom(): void {
    this.zoomedImage = null;
  }
}
