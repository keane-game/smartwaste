import { Component, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatSort, Sort } from '@angular/material/sort';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { SharedService } from '../../../services/shared.service';
import { headerTitleService } from '../../../services/headerTitle.service';
import { ModalService } from '../../../services/modal.service';
import { CreateCircuitCollectComponent } from '../create-circuit-collect/create-circuit-collect.component';
import { DeleteComponent } from '../../../shared/components/delete/delete.component';
import { API_ENDPOINTS } from '../../../shared/constants/api-endpoints';

/**
 * Circuits de collecte.
 *
 * <p>Cet écran était un copier-coller non modifié de l'écran Utilisateurs : il chargeait
 * `/users`, affichait des colonnes de compte (`userLastname`, `userEmail`, `userPhone`…) sous le
 * titre « Gestion des circuits de collecte », et — le plus grave — sa suppression visait
 * `this.sharedService.url`, resté à `/users` : cliquer sur la corbeille d'une ligne SUPPRIMAIT UN
 * UTILISATEUR. La modification, elle, cherchait `item.CcircuitCollectId` (majuscule parasite) sur
 * des objets utilisateur, donc toujours `undefined`. Reconstruit sur la vraie ressource
 * `/v1/circuit-collects`, dont le backend expose le CRUD complet.
 */
@Component({
    selector: 'app-circuit-collect',
    templateUrl: './circuit-collect.component.html',
    styleUrl: './circuit-collect.component.scss',
    standalone: false
})
export class CircuitCollectComponent {

  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = ['name', 'code', 'type', 'frequency', 'rotation', 'length', 'action'];
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
  ) { }

  ngOnInit() {
    this.sharedService.url = API_ENDPOINTS['circuit-collects'].basePath;
    this.headerTitleService.setTitle('Gestion des circuits de collecte');
    this.loadCircuits(this.currentPage, this.itemsPerPage);
  }

  loadCircuits(page: number = 0, size: number = 10): void {
    this.sharedService.url = API_ENDPOINTS['circuit-collects'].basePath;
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

  openCreateCircuitCollecModal() {
    this.modalService.openModal(CreateCircuitCollectComponent, { title: 'Create CircuitCollect' })
      .afterClosed().subscribe(() => this.loadCircuits(this.currentPage, this.itemsPerPage));
  }

  openUpdateCircuitCollectModal(id: any) {
    const currentCircuitCollect = this.dataSource.data.find((item: any) => item.circuitcollectId === id);
    this.modalService.openModal(CreateCircuitCollectComponent, { id: id, currentCircuitCollect: currentCircuitCollect })
      .afterClosed().subscribe(() => this.loadCircuits(this.currentPage, this.itemsPerPage));
  }

  openDeleteCircuitCollecModal(id: any) {
    this.modalService.openModal(DeleteComponent, { id: id, url: API_ENDPOINTS['circuit-collects'].basePath })
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
}
