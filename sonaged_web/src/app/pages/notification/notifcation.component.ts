import { LiveAnnouncer } from '@angular/cdk/a11y';
import { Component, EventEmitter, Input, ViewChild } from '@angular/core';
import { MatDialogRef, MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { SharedService } from '../../services/shared.service';
import { DeleteComponent } from '../../shared/delete/delete.component';
import { CreateDepotoirComponent } from '../depotoir/create-depotoir/create-depotoir.component';
import { headerTitleService } from '../../services/headerTitle.service';
import { first } from 'rxjs';

@Component({
  selector: 'app-notifcation',
  templateUrl: './notifcation.component.html',
  styleUrl: './notifcation.component.scss'
})
export class NotifcationComponent {
OpenUpdateCommuneModal(arg0: any) {
throw new Error('Method not implemented.');
}
OpenCreateCommuneModal() {
throw new Error('Method not implemented.');
}

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  
  displayedColumns: string[] = ['depotoirAddress', 'typeDepotoir', 'quartier', 'longitude', 'latitude', 'action'];
  dataSource: any;
  notifs: any;
  pageNumber: number = 1;
  totalNotifs!: number;
  p: number = 1;
  itemsPerPage: number = 20;
  error = '';
  iSnextPage = false;

  @Input() notifsChangeEvent = new EventEmitter<number>();

  deleteDialogRef!: MatDialogRef<DeleteComponent>;
  createDepotoirDialogRef!: MatDialogRef<CreateDepotoirComponent>;
  constructor(
    private sharedService: SharedService,
    private createDepotoirMatDialog: MatDialog,
    private matDialog: MatDialog,
    private router: Router,
    private _liveAnnouncer: LiveAnnouncer,
    private headerTitleService: headerTitleService
  ) { }

  ngOnInit() {
    this.sharedService.url = '/alert';
    this.sharedService.getAll().subscribe((resp) => {
      this.notifs = resp;
      this.totalNotifs = resp.length;
      this.dataSource = new MatTableDataSource<any>(this.notifs.slice(0, this.itemsPerPage));
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
      console.log(this.notifs);
      this.iSnextPage = this.p < this.notifs.length / this.itemsPerPage
    });
    this.headerTitleService.setTitle('Gestion Dépotoir');
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
 
    if(this.notifs != undefined){
      const totalPages = Math.ceil(this.totalNotifs / this.itemsPerPage);
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
    if (this.p < this.notifs.length / this.itemsPerPage) {
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
    this.dataSource.data = this.notifs.slice(startIndex, endIndex);
  }

  CloseSuccessModal() {
    this.deleteDialogRef.close();
  }

  OpenSuccessModal() {
    this.deleteDialogRef = this.matDialog.open(DeleteComponent, {
      disableClose: false,
      panelClass: ['success-with-dialog'],
    });
  }



  onDeleteUser(id: number): void{
    console.log(id);
    if(confirm('Voulez vous vraiment supprimer cet collaborateur')){
      this.sharedService.delete(+id)
      .pipe(first())
      .subscribe({
        next: () => {
        this.reload("/departments")
        this.OpenSuccessModal()
        setTimeout(()=>  {
          //window.location.reload()
          this.CloseSuccessModal()
        }, 1500 );
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
