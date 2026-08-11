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
import { ListUiState } from '../../../shared/ui/list-ui-state';
import { ManagedColumn } from '../../../shared/components/column-manager/column-manager.component';
import { ColumnPreferencesService } from '../../../shared/ui/column-preferences.service';

@Component({
    selector: 'app-user',
    templateUrl: './user.component.html',
    styleUrls: ['./user.component.scss'],
    standalone: false
})
export class UserComponent  {

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  /** Menu `…`, sélection et bornes de pagination — voir `ListUiState`. */
  readonly ui = new ListUiState();

  /**
   * Inventaire des colonnes proposées par « Gérer les colonnes » (maquettes).
   *
   * <p>`select` et `action` sont verrouillées : les masquer priverait l'écran de la sélection et
   * des actions de ligne sans que l'utilisateur comprenne pourquoi.
   */
  readonly manageableColumns: ManagedColumn[] = [
    { key: 'select', label: 'Sélection', locked: true },
    { key: 'userLastname', label: 'Nom' },
    { key: 'userFirstname', label: 'Prénom' },
    { key: 'userEmail', label: 'Adresse e-mail' },
    { key: 'userRole', label: 'Rôle' },
    { key: 'userAddress', label: 'Adresse' },
    { key: 'userPhone', label: 'Téléphone' },
    { key: 'action', label: 'Actions', locked: true },
  ];

  /** Clé de persistance : le choix de colonnes est propre à l'utilisateur. */
  private static readonly COLUMNS_KEY = 'users.columns';

  // Renseignée dans `ngOnInit` : `columnPrefs` est injecté par le constructeur.
  displayedColumns: string[] = [];
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
    private columnPrefs: ColumnPreferencesService,
  ) { }


  ngOnInit() {
    this.displayedColumns = this.columnPrefs.restore(
      UserComponent.COLUMNS_KEY, this.manageableColumns, ['userAddress']);
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

  // `afterClosed` : sans rechargement, la liste restait figée après une création, une
  // modification ou une suppression — l'utilisateur croyait l'action sans effet.
  openCreateUserModal() {
    this.modalService.openModal(CreateUserComponent, { title: 'Create User' })
      .afterClosed().subscribe(() => this.loadUsers(this.currentPage, this.itemsPerPage));
  }

  openUpdateUserModal(id: any) {
    const currentUser = this.dataSource.data.find((item: any) => item.userId === id);
    this.modalService.openModal(CreateUserComponent, { id: id, currentUser: currentUser })
      .afterClosed().subscribe(() => this.loadUsers(this.currentPage, this.itemsPerPage));
  }

  openDeleteUserModal(id:any) {
    this.modalService.openModal(DeleteComponent, { id: id, url: this.sharedService.url })
      .afterClosed().subscribe(() => this.loadUsers(this.currentPage, this.itemsPerPage));
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




  goToPage(pageIndex: number): void {
    if (pageIndex < 0 || pageIndex >= this.totalPages) { return; }
    this.onPaginatedChange({ pageIndex, pageSize: this.itemsPerPage });
  }

  /**
   * Rôle affiché.
   *
   * <p>Le DTO expose l'autorité sous `authority.name` (« SUPER_ADMIN ») — vérifié sur la réponse
   * réelle de `GET /v1/users`. Les champs sont préfixés `user…` côté serveur (`userLastname`,
   * `userEmail`…), ce qui ne se devine pas : les lier sans vérifier affichait un tableau
   * entièrement vide.
   */
  roleLabel(user: any): string {
    return user?.authority?.name ?? '—';
  }

  openColumnManager(): void {
    this.columnPrefs.open(UserComponent.COLUMNS_KEY, this.manageableColumns, this.displayedColumns)
      .subscribe(columns => { if (columns) { this.displayedColumns = columns; } });
  }
}
