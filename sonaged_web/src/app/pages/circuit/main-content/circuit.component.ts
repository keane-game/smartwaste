import { LiveAnnouncer } from '@angular/cdk/a11y';
import { SelectionModel } from '@angular/cdk/collections';
import { Component, EventEmitter, Input, ViewChild } from '@angular/core';
import { MatDialogRef, MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { Router } from '@angular/router';
import { User } from '../../../models/user.model';
import { SharedService } from '../../../services/shared.service';
import { DeleteComponent } from '../../../shared/delete/delete.component';
import { CreateCircuitComponent } from '../create-circuit/create-circuit.component';
import { headerTitleService } from '../../../services/headerTitle.service';
import { MatTableDataSource } from '@angular/material/table';
import { first } from 'rxjs';

@Component({
  selector: 'app-circuit',
  templateUrl: './circuit.component.html',
  styleUrl: './circuit.component.scss'
})
export class CircuitComponent {


  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = ['depotoirAddress', 'typeDepotoir', 'quartier', 'longitude', 'latitude', 'action'];
  dataSource: any;
  selection = new SelectionModel<User>(true, []);
  circuits: any;
  pageNumber: number = 1;
  totalCircuits!: number;
  p: number = 1;
  itemsPerPage: number = 20;
  error = '';
  iSnextPage = false;


  @Input() circuitChangeEvent = new EventEmitter<number>();

  deleteDialogRef!: MatDialogRef<DeleteComponent>;
  createCircuitDialogRef!: MatDialogRef<CreateCircuitComponent>;
  constructor(
    private sharedService: SharedService,
    private createCircuitMatDialog: MatDialog,
    private updateUserMatDialog: MatDialog,
    private matDialog: MatDialog,
    private router: Router,
    private _liveAnnouncer: LiveAnnouncer,
    private headerTitleService: headerTitleService
  ) { }

  ngOnInit() {
    this.sharedService.url = '/circuits/all';
    this.sharedService.getAll().subscribe((resp) => {
      this.circuits = resp;
      this.totalCircuits = resp.length;
      this.dataSource = new MatTableDataSource<any>(this.circuits.slice(0, this.itemsPerPage));
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
      //console.log(this.users);
      this.iSnextPage = this.p < this.circuits.length / this.itemsPerPage
    });
    this.headerTitleService.setTitle('Gestion Dépotoir');
  }
  OpenCreateCommuneModal() {
    this.closeDialog();
    this.createCircuitDialogRef = this.createCircuitMatDialog.open(CreateCircuitComponent, {
      disableClose: true,
      panelClass: ['md:w-5/5', 'w-full', 'full-with-dialog'],
      maxHeight: '100vh',
      maxWidth: '100%'

    });
  }


  OpenUpdateCommuneModal(arg0: any) {
    throw new Error('Method not implemented.');
  }
    

  async closeDialog() {
    try {
      this.createCircuitMatDialog.closeAll(); // make sure it only closes if the upper async fn succesfully ran!
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
 
     if(this.circuits != undefined){
       const totalPages = Math.ceil(this.totalCircuits / this.itemsPerPage);
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
     if (this.p < this.circuits.length / this.itemsPerPage) {
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
     this.dataSource.data = this.circuits.slice(startIndex, endIndex);
   }
 
   changeEditIcon(event: Event): void {
     const target = event.target as HTMLImageElement;
     if (target) {
       if (event.type === 'mouseover') {
         target.src = '../../assets/icons/icon-edit.svg';
       } else if (event.type === 'mouseout') {
         target.src = '../../assets/icons/icon-edit-inactive.svg';
       }
     }
   }
 
   changeDeleteIcon(event: Event): void {
     const target = event.target as HTMLImageElement;
     if (target) {
       if (event.type === 'mouseover') {
         target.src = '../../assets/icons/icon-delete.svg';
       } else if (event.type === 'mouseout') {
         target.src = '../../assets/icons/icon-delete-inactive.svg';
       }
     }
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
     this.sharedService.url = '/delete/circuit';
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
