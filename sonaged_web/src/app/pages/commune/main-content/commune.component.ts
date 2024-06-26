import { LiveAnnouncer } from '@angular/cdk/a11y';
import { SelectionModel } from '@angular/cdk/collections';
import { Component, EventEmitter, Input, ViewChild } from '@angular/core';
import { MatDialogRef, MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { first } from 'rxjs';
import { User } from '../../../models/user.model';
import { SharedService } from '../../../services/shared.service';
import { DeleteComponent } from '../../../shared/delete/delete.component';
import { CreateDepartmentComponent } from '../../department/create-department/create-department.component';
import { CreateCommuneComponent } from '../create-commune/create-commune.component';
import { headerTitleService } from '../../../services/headerTitle.service';

@Component({
  selector: 'app-commune',
  templateUrl: './commune.component.html',
  styleUrl: './commune.component.scss'
})
export class CommuneComponent {


  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = ['communeName', 'communeCode', 'department', 'totalResident', 'womanResident', 'manResident', 'communeLength', 'communeArea', 'action'];
  dataSource: any;
  selection = new SelectionModel<User>(true, []);
  communes: any;
  pageNumber: number = 1;
  totalCommunes!: number;
  p: number = 1;
  itemsPerPage: number = 20;
  error = '';
  iSnextPage = false;

  @Input() userChangeEvent = new EventEmitter<number>();

  deleteDialogRef!: MatDialogRef<DeleteComponent>;
  createCommuneDialogRef!: MatDialogRef<CreateCommuneComponent>;
  constructor(
    private sharedService: SharedService,
    private createCommuneMatDialog: MatDialog,
    private matDialog: MatDialog,
    private router: Router,
    private _liveAnnouncer: LiveAnnouncer,
    private headerTitleService: headerTitleService
  ) { }

  ngOnInit() {
    this.sharedService.url = '/communes';
    this.sharedService.getAll().subscribe((resp) => {
      this.communes = resp;
      this.totalCommunes = resp.length;
      this.dataSource = new MatTableDataSource<any>(this.communes.slice(0, this.itemsPerPage));
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
      console.log(this.communes);
      this.iSnextPage = this.p < this.communes.length / this.itemsPerPage
    });
    this.headerTitleService.setTitle('Gestion Commune');
  }

  
  OpenCreateCommuneModal() {
    this.closeDialog();
    this.createCommuneDialogRef = this.createCommuneMatDialog.open(CreateCommuneComponent, {
      disableClose: true,
      panelClass: ['md:w-5/5', 'w-full', 'full-with-dialog'],
      maxHeight: '100vh',
      maxWidth: '100%'

    });
  }

    
  OpenUpdateCommuneModal(id: any) {
    this.closeDialog();
    this.createCommuneDialogRef = this.createCommuneMatDialog.open(CreateCommuneComponent, {
      disableClose: true,
      panelClass: ['md:w-5/5', 'w-full', 'full-with-dialog'],
      maxHeight: '100vh',
      maxWidth: '100%'
    });

    this.createCommuneDialogRef.componentInstance.id = id;
    this.createCommuneDialogRef.componentInstance
    .currentCommune = this.communes.filter((item: any)=>item.communeId == id)[0];
     //console.log(this.users, id);
  }
    

  async closeDialog() {
    try {
      this.createCommuneMatDialog.closeAll(); // make sure it only closes if the upper async fn succesfully ran!
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
 
     if(this.communes != undefined){
       const totalPages = Math.ceil(this.totalCommunes / this.itemsPerPage);
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
     if (this.p < this.communes.length / this.itemsPerPage) {
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
     this.dataSource.data = this.communes.slice(startIndex, endIndex);
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
     this.sharedService.url = '/delete/commune';
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
