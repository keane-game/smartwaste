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

@Component({
  selector: 'app-quartier',
  templateUrl: './quartier.component.html',
  styleUrl: './quartier.component.scss'
})
export class QuartierComponent {

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = ['quartierName', 'quartierCode', 'quartierLength', 'quartierArea', 'quartierZoneCoron', 'action'];
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
    private modalService: ModalService
  ) { }

  ngOnInit() {
    this.sharedService.url = '/quartiers';
    this.headerTitleService.setTitle('Gestion Quartier');
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
    this.modalService.openModal(CreateQuartierComponent, { title: 'Create Quartier' });
  }

  openUpdateQuartierModal(id: any) {
    const currentQuartier = this.dataSource.data.find((item: any) => item.quartierId === id);
    //console.log(id)
    this.modalService.openModal(CreateQuartierComponent, { id: id, currentQuartier: currentQuartier });
  }

  openDeleteQuartierModal(id:any) {
    this.modalService.openModal(DeleteComponent, { id: id, url: this.sharedService.url });
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
}
