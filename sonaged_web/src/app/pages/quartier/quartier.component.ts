import { LiveAnnouncer } from '@angular/cdk/a11y';
import { SelectionModel } from '@angular/cdk/collections';
import { Component, EventEmitter, Input, ViewChild } from '@angular/core';
import { MatDialogRef, MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { Router } from '@angular/router';
import { SharedService } from '../../services/shared.service';
import { DeleteComponent } from '../../shares/delete/delete.component';
import { CreateQuartierComponent } from '../quartier/create-quartier/create-quartier.component';
import { headerTitleService } from '../../services/headerTitle.service';
import { MatTableDataSource } from '@angular/material/table';
import { first } from 'rxjs';

@Component({
  selector: 'app-quartier',
  templateUrl: './quartier.component.html',
  styleUrl: './quartier.component.scss'
})
export class QuartierComponent {

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = ['quartierName', 'quartierCode', 'quartierLength', 'quartierArea', 'quartierZoneCoron', 'action'];
  dataSource: any;
  selection = new SelectionModel<any>(true, []);
  quartiers: any;
  pageNumber: number = 1;
  totalQuartiers!: number;
  p: number = 1;
  itemsPerPage: number = 20;
  error = '';
  iSnextPage = false;

  @Input() circuitChangeEvent = new EventEmitter<number>();

  deleteDialogRef!: MatDialogRef<DeleteComponent>;
  createQuartierDialogRef!: MatDialogRef<CreateQuartierComponent>;
  constructor(
    private sharedService: SharedService,
    private createQuartierMatDialog: MatDialog,
    private matDialog: MatDialog,
    private router: Router,
    private _liveAnnouncer: LiveAnnouncer,
    private headerTitleService: headerTitleService
  ) { }

  ngOnInit() {
    this.sharedService.url = '/quartier';
    this.sharedService.getAll().subscribe((resp) => {
      this.quartiers = resp;
      this.totalQuartiers = resp.length;
      this.dataSource = new MatTableDataSource<any>(this.quartiers.slice(0, this.itemsPerPage));
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
      console.log(this.quartiers);
      this.iSnextPage = this.p < this.quartiers.length / this.itemsPerPage
    });
    this.headerTitleService.setTitle('Gestion Quartier');
  }

  OpenCreateQuartierModal() {
    this.closeDialog();
    this.createQuartierDialogRef = this.createQuartierMatDialog.open(CreateQuartierComponent, {
      disableClose: true,
      panelClass: ['md:w-5/5', 'w-full', 'full-with-dialog'],
      maxHeight: '100vh',
      maxWidth: '100%'

    });
  }

  OpenUpdateQuartierModal(id: any) {
    this.closeDialog();
    this.createQuartierDialogRef = this.createQuartierMatDialog.open(CreateQuartierComponent, {
      disableClose: true,
      panelClass: ['md:w-5/5', 'w-full', 'full-with-dialog'],
      maxHeight: '100vh',
      maxWidth: '100%'
    });

    this.createQuartierDialogRef.componentInstance.id = id;
    this.createQuartierDialogRef.componentInstance
    .currentQuartier = this.quartiers.filter((item: any)=>item.quartierId == id)[0];
     //console.log(this.users, id);
  }
    



  async closeDialog() {
    try {
      this.createQuartierMatDialog.closeAll(); // make sure it only closes if the upper async fn succesfully ran!
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
 
     if(this.quartiers != undefined){
       const totalPages = Math.ceil(this.totalQuartiers / this.itemsPerPage);
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
     if (this.p < this.quartiers.length / this.itemsPerPage) {
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
     this.dataSource.data = this.quartiers.slice(startIndex, endIndex);
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
     this.sharedService.url = '/commune';
     if(confirm('Voulez vous vraiment supprimer cet commune')){
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
