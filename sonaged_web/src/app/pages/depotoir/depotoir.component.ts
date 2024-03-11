import { Component, EventEmitter, Input, ViewChild } from '@angular/core';
import { headerTitleService } from '../../services/headerTitle.service';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { SelectionModel } from '@angular/cdk/collections';
import { MatDialogRef, MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { first } from 'rxjs';
import { SharedService } from '../../services/shared.service';
import { DeleteComponent } from '../../shares/delete/delete.component';
import { CreateDepotoirComponent } from './create/create-depotoir.component';

@Component({
  selector: 'app-depotoir',
  templateUrl: './depotoir.component.html',
  styleUrls: ['./depotoir.component.scss']
})
export class DepotoirComponent {


  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = ['depotoirAddress', 'typeDepotoir', 'quartier', 'longitude', 'latitude', 'action'];
  dataSource: any;
  depotoirs: any;
  pageNumber: number = 1;
  totalDepotoirs!: number;
  p: number = 1;
  itemsPerPage: number = 20;
  error = '';
  iSnextPage = false;

  @Input() depotChangeEvent = new EventEmitter<number>();

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
    this.sharedService.url = '/depotoir';
    this.sharedService.getAll().subscribe((resp) => {
      this.depotoirs = resp;
      this.totalDepotoirs = resp.length;
      this.dataSource = new MatTableDataSource<any>(this.depotoirs.slice(0, this.itemsPerPage));
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
      console.log(this.depotoirs);
      this.iSnextPage = this.p < this.depotoirs.length / this.itemsPerPage
    });
    this.headerTitleService.setTitle('Gestion Dépotoir');
  }
  OpenCreateCommuneModal() {
    this.closeDialog();
    this.createDepotoirDialogRef = this.createDepotoirMatDialog.open(CreateDepotoirComponent, {
      disableClose: true,
      panelClass: ['md:w-5/5', 'w-full', 'full-with-dialog'],
      maxHeight: '100vh',
      maxWidth: '100%'

    });
  }

  OpenUpdateCommuneModal(id: any) {
    this.closeDialog();
    this.createDepotoirDialogRef = this.createDepotoirMatDialog.open(CreateDepotoirComponent, {
      disableClose: true,
      panelClass: ['md:w-5/5', 'w-full', 'full-with-dialog'],
      maxHeight: '100vh',
      maxWidth: '100%'
    });

    this.createDepotoirDialogRef.componentInstance.id = id;
    this.createDepotoirDialogRef.componentInstance
    .currentDepotoir = this.depotoirs.filter((item: any)=>item.depotoirId == id)[0];
     //console.log(this.users, id);
  }
    
    

  async closeDialog() {
    try {
      this.createDepotoirMatDialog.closeAll(); // make sure it only closes if the upper async fn succesfully ran!
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
 
     if(this.depotoirs != undefined){
       const totalPages = Math.ceil(this.totalDepotoirs / this.itemsPerPage);
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
     if (this.p < this.depotoirs.length / this.itemsPerPage) {
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
     this.dataSource.data = this.depotoirs.slice(startIndex, endIndex);
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
