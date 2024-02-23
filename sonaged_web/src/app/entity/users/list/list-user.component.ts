import { Component, OnInit, ViewChild, Input, EventEmitter } from '@angular/core';
import { User } from '../../../models/user.model'
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { SelectionModel } from '@angular/cdk/collections';
import { UserService } from '../../../services/user.service';
import { SharedService } from '../../../services/shared.service';
import { first } from 'rxjs';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { headerTitleService } from '../../../services/headerTitle.service';
import { CreateUserComponent } from '../create/create-user.component';
import { UpdateUserComponent } from '../update/update-user.component';

@Component({
  selector: 'app-users',
  templateUrl: './list-user.component.html',
  styleUrls: ['./list-user.component.scss']
})
export class ListUserComponent implements  OnInit {

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = ['userLastname', 'userFirstname', 'userEmail', 'userAddress', 'userPhone', 'action'];
  dataSource: any;
  selection = new SelectionModel<User>(true, []);
  users: any;
  pageNumber: number = 1;
  totalUsers!: number;
  p: number = 1;
  itemsPerPage: number = 20;
  error = '';
  iSnextPage = false;

  @Input() userChangeEvent = new EventEmitter<number>();

  createUserDialogRef!: MatDialogRef<CreateUserComponent>;
  updateUserDialogRef!: MatDialogRef<UpdateUserComponent>;
  constructor(
    private userService: UserService,
    private sharedService: SharedService,
    private createUserMatDialog: MatDialog,
    private updateUserMatDialog: MatDialog,
    private _liveAnnouncer: LiveAnnouncer,
    private headerTitleService: headerTitleService
    ) { }

    
  ngOnInit() {
    this.sharedService.url = '/users/all';
    this.sharedService.getAll().subscribe((resp) => {
      this.users = resp;
      this.totalUsers = resp.length;
      this.dataSource = new MatTableDataSource<User>(this.users.slice(0, this.itemsPerPage));
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
      //console.log(this.users);
      this.iSnextPage = this.p < this.users.length / this.itemsPerPage
    });
    this.headerTitleService.setTitle('Gestion Utilisateur');
  }


  OpenCreateUserModal() {
    this.closeDialog();
    this.createUserDialogRef = this.createUserMatDialog.open(CreateUserComponent, {
      disableClose: true,
      panelClass: ['md:w-5/5', 'w-full', 'full-with-dialog'],
      maxHeight: '100vh',
      maxWidth: '100%'

    });
  }

 

  OpenUpdateUserModal(id: any) {
    this.closeDialog();
    this.updateUserDialogRef = this.updateUserMatDialog.open(UpdateUserComponent, {
      disableClose: true,
      panelClass: ['md:w-5/5', 'w-full', 'full-with-dialog'],
      maxHeight: '100vh',
      maxWidth: '100%'
    });
    this.updateUserDialogRef.componentInstance.id = id;
    this.updateUserDialogRef.componentInstance
    .currentUser = this.users.filter((item: any)=>item.userId == id)[0];
   // console.log(this.users, id);
  }



  async closeDialog() {
    try {
      this.createUserMatDialog.closeAll(); // make sure it only closes if the upper async fn succesfully ran!
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

    if(this.users != undefined){
      const totalPages = Math.ceil(this.totalUsers / this.itemsPerPage);
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
    if (this.p < this.users.length / this.itemsPerPage) {
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
    this.dataSource.data = this.users.slice(startIndex, endIndex);
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

  onDeleteUser(id: number): void{
    console.log(id);
    this.sharedService.url = '/delete/user';
    if(confirm('Voulez vous vraiment supprimer cet collaborateur')){
      this.sharedService.delete(+id)
      .pipe(first())
      .subscribe({
        next: (response) => {
        //  console.log(response);
          this.ngOnInit();
        },
        error:  error => { this.error = error}
      })
        
    }
  }

}
