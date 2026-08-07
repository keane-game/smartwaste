import { Component, OnInit, ViewChild, Input, EventEmitter } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { SelectionModel } from '@angular/cdk/collections';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { Router } from '@angular/router';

import { ModalService } from '../../../services/modal.service';
import { headerTitleService } from '../../../services/headerTitle.service';
import { SharedService } from '../../../services/shared.service';

import { User } from '../../../models/user.model'
import { CreateUserComponent } from '../create-user/create-user.component';
import { DeleteComponent } from '../../../shared/components/delete/delete.component';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss']
})
export class UserComponent  {

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = ['userLastname', 'userFirstname', 'userEmail', 'userAddress', 'userPhone', 'action'];
  dataSource = new MatTableDataSource<any>([]);
  selection = new SelectionModel<User>(true, []);

  users: any;
  totalPages: number = 1;
  totalUsers: number = 0;
  pageSizeOptions: number[] = [5, 10, 20];
  itemsPerPage: number = 10;
  currentPage: number = 0;
  error = '';


  @Input() userChangeEvent = new EventEmitter<number>();

  constructor(
    private sharedService: SharedService,
    private router: Router,
    private _liveAnnouncer: LiveAnnouncer,
    private headerTitleService: headerTitleService,
    private modalService: ModalService,
  ) { }


  ngOnInit() {
    this.sharedService.url = '/users';
    this.loadUsers(this.currentPage, this.itemsPerPage);
    this.headerTitleService.setTitle('Gestion des utilisateurs');
  }

  loadUsers(page: number = 0, size: number = 10): void {
    this.sharedService.getResources(page, size).subscribe((resp) => {
      this.dataSource.data = resp.content;
      this.totalUsers = resp.totalElements;
      this.dataSource.paginator = this.paginator;
      this.totalPages = Math.ceil(this.totalUsers / this.itemsPerPage);
      this.dataSource.sort = this.sort;
    //  console.log( this.currentPage);
    });
  }


  onPaginatedChange(event: { pageIndex: number, pageSize: number }) {
    this.currentPage = event.pageIndex;
    this.itemsPerPage = event.pageSize;
    this.loadUsers(this.currentPage, this.itemsPerPage);
    console.log(event);
  }

  openCreateUserModal() {
    this.modalService.openModal(CreateUserComponent, { title: 'Create User' });
  }

  openUpdateUserModal(id: any) {
    const currentUser = this.dataSource.data.find((item: any) => item.userId === id);
    //console.log(id)
    this.modalService.openModal(CreateUserComponent, { id: id, currentUser: currentUser });
  }

  openDeleteUserModal(id:any) {
    this.modalService.openModal(DeleteComponent, { id: id, url: this.sharedService.url });
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



}
