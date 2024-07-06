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

@Component({
  selector: 'app-commune',
  templateUrl: './commune.component.html',
  styleUrl: './commune.component.scss'
})
export class CommuneComponent {


  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = ['communeName', 'communeCode', 'department', 'totalResident', 'womanResident', 'manResident', 'communeLength', 'communeArea', 'action'];
  dataSource = new MatTableDataSource<any>([]);
  selection = new SelectionModel<any>(true, []);

  communes: any;
  totalPages: number = 1;
  totalCommunes: number = 0;
  pageSizeOptions: number[] = [5, 10, 20];
  itemsPerPage: number = 10;
  currentPage: number = 0;
  error = '';

  @Input() communeChangeEvent = new EventEmitter<number>();

  constructor(
    private sharedService: SharedService,
    private router: Router,
    private _liveAnnouncer: LiveAnnouncer,
    private headerTitleService: headerTitleService,
    private modalService: ModalService
  ) { }

  ngOnInit() {
    this.sharedService.url = '/communes';
    this.loadCommuns(this.currentPage, this.itemsPerPage);
    this.headerTitleService.setTitle('Gestion Utilisateur');
  }

  loadCommuns(page: number = 0, size: number = 10): void {
    this.sharedService.getResources(page, size).subscribe((resp) => {
      this.dataSource.data = resp.content;
      this.totalCommunes = resp.totalElements;
      this.dataSource.paginator = this.paginator;
      this.totalPages = Math.ceil(this.totalCommunes / this.itemsPerPage);
      this.dataSource.sort = this.sort;

      console.log( this.currentPage);
    });
  }
  
  onPaginatedChange(event: { pageIndex: number, pageSize: number }) {
    this.currentPage = event.pageIndex;
    this.itemsPerPage = event.pageSize;
    this.loadCommuns(this.currentPage, this.itemsPerPage);
    console.log(event);
  }

  openCreateCommuneModal() {
    this.modalService.openModal(CreateCommuneComponent, { title: 'Create Commune' });
  }

  openUpdateCommuneModal(id: any) {
    const currentCommune = this.dataSource.data.find((item: any) => item.communId === id);
    //console.log(id)
    this.modalService.openModal(CreateCommuneComponent, { id: id, currentCommun: currentCommune });
  }

  openDeleteCommuneModal(id:any) {
    this.modalService.openModal(DeleteComponent, { id: id, url: this.sharedService.url});
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
}
