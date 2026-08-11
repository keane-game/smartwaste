import { Component, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { MatTableDataSource } from '@angular/material/table';
import { first } from 'rxjs';
import { SharedService } from '../../services/shared.service';
import { API_ENDPOINTS } from '../../shared/constants/api-endpoints';
import { headerTitleService } from '../../services/headerTitle.service';
import { ModalService } from '../../services/modal.service';
import { DeleteComponent } from '../../shared/components/delete/delete.component';
import { CreateDepartmentComponent } from './create-department/create-department.component';

@Component({
    selector: 'app-department',
    templateUrl: './department.component.html',
    styleUrl: './department.component.scss',
    standalone: false
})
export class DepartmentComponent {

  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = ['departmentName', 'departmentCode', 'region', 'commune', 'action'];
  dataSource = new MatTableDataSource<any>([]);

  totalPages: number = 1;
  totalDeparts: number = 0;
  itemsPerPage: number = 10;
  pageSizeOptions: number[] = [5, 10, 20];
  currentPage: number = 0;
  error = '';

  constructor(
    private sharedService: SharedService,
    private modalService: ModalService,
    private _liveAnnouncer: LiveAnnouncer,
    private headerTitleService: headerTitleService,
  ) { }

  ngOnInit() {
    // `basePath` (chemin nu paginé), pas `listPath` (`/s`, liste complète) : `loadDeparts` a
    // besoin de la réponse `Page<Department>` avec `content`/`totalElements`.
    this.sharedService.url = API_ENDPOINTS.departments.basePath;
    this.headerTitleService.setTitle('Gestion des départements');
    this.loadDeparts(this.currentPage, this.itemsPerPage);
  }

  loadDeparts(page: number = 0, size: number = 10): void {
    this.sharedService.getResources(page, size).subscribe((resp) => {
      this.dataSource.data = resp.content;
      this.totalDeparts = resp.totalElements;
      this.totalPages = Math.ceil(this.totalDeparts / this.itemsPerPage);
      this.dataSource.sort = this.sort;
    });
  }

  onPaginatedChange(event: { pageIndex: number, pageSize: number }) {
    this.currentPage = event.pageIndex;
    this.itemsPerPage = event.pageSize;
    this.loadDeparts(this.currentPage, this.itemsPerPage);
  }

  openCreateDepartModal() {
    this.modalService.openModal(CreateDepartmentComponent, { title: 'Create Department' })
      .afterClosed().subscribe(() => this.loadDeparts(this.currentPage, this.itemsPerPage));
  }

  openUpdateDepartModal(id: any) {
    const currentDepart = this.dataSource.data.find((item: any) => item.departmentId === id);
    this.modalService.openModal(CreateDepartmentComponent, { id: id, currentDepart: currentDepart })
      .afterClosed().subscribe(() => this.loadDeparts(this.currentPage, this.itemsPerPage));
  }

  openDeleteDepartModal(id: any) {
    this.modalService.openModal(DeleteComponent, { id: id, url: API_ENDPOINTS.departments.basePath })
      .afterClosed().subscribe(() => this.loadDeparts(this.currentPage, this.itemsPerPage));
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
