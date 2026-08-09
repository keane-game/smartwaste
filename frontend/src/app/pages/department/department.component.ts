import { LiveAnnouncer } from '@angular/cdk/a11y';
import { SelectionModel } from '@angular/cdk/collections';
import { Component, EventEmitter, Input, ViewChild, ViewEncapsulation } from '@angular/core';
import { MatDialogRef, MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { Router } from '@angular/router';
import { User } from '../../models/user.model';
import { SharedService } from '../../services/shared.service';
import { API_ENDPOINTS } from '../../shared/constants/api-endpoints';
import { headerTitleService } from '../../services/headerTitle.service';
import { MatTableDataSource } from '@angular/material/table';
import { first } from 'rxjs';

import { CreateDepartmentComponent } from './create-department/create-department.component';

@Component({
    selector: 'app-department',
    templateUrl: './department.component.html',
    styleUrl: './department.component.scss',
    standalone: false
})
export class DepartmentComponent {



  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = ['departmentName', 'departmentCode', 'region', 'commune', 'action'];
  dataSource: any;
  selection = new SelectionModel<User>(true, []);
  departs: any;
  pageNumber: number = 1;
  totalDeparts!: number;
  p: number = 1;
  itemsPerPage: number = 20;
  error = '';
  iSnextPage = false;

  @Input() userChangeEvent = new EventEmitter<number>();
  createDepartDialogRef!: MatDialogRef<CreateDepartmentComponent>;
  constructor(
    private sharedService: SharedService,
    private createDepartMatDialog: MatDialog,
    private updateDepartMatDialog: MatDialog,
    private matDialog: MatDialog,
    private router: Router,
    private _liveAnnouncer: LiveAnnouncer,
    private headerTitleService: headerTitleService
    ) { }

    ngOnInit() {
      this.sharedService.url = API_ENDPOINTS.departments.listPath;
      this.sharedService.getAll().subscribe((resp) => {
        this.departs = resp;
        this.totalDeparts = resp.length;
        this.dataSource = new MatTableDataSource<any>(this.departs.slice(0, this.itemsPerPage));
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
        console.log(this.departs);
        this.iSnextPage = this.p < this.departs.length / this.itemsPerPage
      });
      this.headerTitleService.setTitle('Gestion des départements');
    }
  
  
    OpenCreateDepartModal() {
      this.closeDialog();
      this.createDepartDialogRef = this.createDepartMatDialog.open(CreateDepartmentComponent, {
        disableClose: true,
        panelClass: ['md:w-5/5', 'w-full', 'full-with-dialog'],
        maxHeight: '100vh',
        maxWidth: '100%'
  
      });
    }


    OpenUpdateDepartModal(id: any) {
    this.closeDialog();
    this.createDepartDialogRef = this.updateDepartMatDialog.open(CreateDepartmentComponent, {
      disableClose: true,
      panelClass: ['md:w-5/5', 'w-full', 'full-with-dialog'],
      maxHeight: '100vh',
      maxWidth: '100%'
    });
    this.createDepartDialogRef.componentInstance.id = id;
    this.createDepartDialogRef.componentInstance
    .currentDepart = this.departs.filter((item: any)=>item.departmentId == id)[0];
     //console.log(this.users, id);
  }
    


  async closeDialog() {
    try {
      this.createDepartMatDialog.closeAll(); // make sure it only closes if the upper async fn succesfully ran!
    } catch($e) {
      
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

  // Fonction pour obtenir les numéros de page
  getPages(currentPage: number): number[] {

    if(this.departs != undefined){
      const totalPages = Math.ceil(this.totalDeparts / this.itemsPerPage);
      if (totalPages <= 3) {
        return Array(totalPages).fill(0).map((_, i) => i + 1);
      } else if (currentPage === 1) {
        return [1, 2, 3];
      } else if (currentPage === totalPages) {
        return [currentPage - 2, currentPage - 1, currentPage];
      } else {
        return [currentPage - 1, currentPage, currentPage + 1];
      }
    }else {
      return []
    }
 
  }

  previousPage(): void {
    if (this.p > 1) {
      this.p--;
      this.updateDataSource();
    }
  }

  nextPage(): void {
    if (this.p < this.departs.length / this.itemsPerPage) {
      this.p++;
      this.updateDataSource();
    }
  }

  goToPage(page: number): void {
    this.p = page;
    this.updateDataSource();
  }

  updateDataSource(): void {
    const startIndex = (this.p - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.dataSource.data = this.departs.slice(startIndex, endIndex);
  }


  onDeleteUser(id: string): void{
    console.log(id);
    this.sharedService.url = API_ENDPOINTS.departments.basePath;
    if(confirm('Voulez vous vraiment supprimer ce département')){
      this.sharedService.delete(id)
      .pipe(first())
      .subscribe({
        next: () => {
       
        },
        error:  error => { this.error = error}
      })
        
    }
  }

  async reload(url: string): Promise<boolean> {
    await this.router.navigateByUrl('/', { skipLocationChange: true });
    return this.router.navigateByUrl(url);
  }


}
