import { Component, EventEmitter, Input, ViewChild } from '@angular/core';
import { headerTitleService } from '../../../services/headerTitle.service';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { SelectionModel } from '@angular/cdk/collections';
import { MatDialogRef, MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { first } from 'rxjs';
import { SharedService } from '../../../services/shared.service';
import { DeleteComponent } from '../../../shared/delete/delete.component';
import { CreateDepotoirComponent } from '../create-depotoir/create-depotoir.component';
import { ModalService } from '../../../services/modal.service';
import { DeleteDepotoirComponent } from '../delete-depotoir/delete-depotoir.component';

@Component({
  selector: 'app-depotoir',
  templateUrl: './depotoir.component.html',
  styleUrls: ['./depotoir.component.scss']
})
export class DepotoirComponent {


  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = ['depotoirAddress', 'typeDepotoir', 'commune', 'longitude', 'latitude', 'action'];
  dataSource = new MatTableDataSource<any>([]);
  selection = new SelectionModel<any>(true, []);

  depotoirs: any;
  totalPages: number = 1;
  totalDepotoirs: number = 0;
  itemsPerPage: number = 10;
  currentPage: number = 0;
  error = '';

  @Input() depotChangeEvent = new EventEmitter<number>();

 constructor(
    private sharedService: SharedService,
    private router: Router,
    private _liveAnnouncer: LiveAnnouncer,
    private headerTitleService: headerTitleService,
    private modalService: ModalService
  ) { }


  ngOnInit() {
    this.sharedService.url = '/depotoirs';
    this.loadDepotoirs();
    this.headerTitleService.setTitle('Gestion Dépotoir');
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
    this.modalService.openModal(CreateDepotoirComponent, { title: 'Create Depotoir' });
  }

  openUpdateDepotoirModal(id: any) {
    const currentDepotoir = this.dataSource.data.find((item: any) => item.depotoirId === id);
    //console.log(id)
    this.modalService.openModal(CreateDepotoirComponent, { id: id, currentDepotoir: currentDepotoir });
  }

  openDeleteDepotoirModal(id:any) {
    this.modalService.openModal(DeleteDepotoirComponent, { id: id});
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

  
}
