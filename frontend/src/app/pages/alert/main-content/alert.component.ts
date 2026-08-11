import { Component, ViewChild } from '@angular/core';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { SharedService } from '../../../services/shared.service';
import { headerTitleService } from '../../../services/headerTitle.service';
import { ModalService } from '../../../services/modal.service';
import { CreateAlertComponent } from '../create-alert/create-alert.component';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { DeleteComponent } from '../../../shared/components/delete/delete.component';
import { API_ENDPOINTS } from '../../../shared/constants/api-endpoints';
import { ListUiState } from '../../../shared/ui/list-ui-state';
import { ManagedColumn } from '../../../shared/components/column-manager/column-manager.component';
import { ColumnPreferencesService } from '../../../shared/ui/column-preferences.service';

/** Onglet de filtre : `null` = toutes gravités confondues. */
interface GravityTab {
  label: string;
  code: string | null;
}

@Component({
    selector: 'app-alert',
    templateUrl: './alert.component.html',
    styleUrl: './alert.component.scss',
    standalone: false
})
export class AlertComponent {

  @ViewChild(MatSort) sort!: MatSort;

  /**
   * Inventaire des colonnes proposées par « Gérer les colonnes » (maquettes).
   *
   * <p>`select` et `action` sont verrouillées : les masquer priverait l'écran de la sélection et
   * des actions de ligne sans que l'utilisateur comprenne pourquoi.
   */
  readonly manageableColumns: ManagedColumn[] = [
    { key: 'select', label: 'Sélection', locked: true },
    { key: 'picture', label: 'Image' },
    { key: 'object', label: 'Objet' },
    { key: 'message', label: 'Description' },
    { key: 'code', label: 'Gravité' },
    { key: 'adress', label: 'Adresse' },
    { key: 'action', label: 'Actions', locked: true },
  ];

  /** Clé de persistance : le choix de colonnes est propre à l'utilisateur. */
  private static readonly COLUMNS_KEY = 'alerts.columns';

  // Renseignée dans `ngOnInit` : `columnPrefs` est injecté par le constructeur et n'existe pas
  // encore au moment où les champs s'initialisent.
  displayedColumns: string[] = [];
  dataSource = new MatTableDataSource<any>([]);

  readonly ui = new ListUiState();

  totalPages: number = 1;
  totalAlerts: number = 0;
  pageSizeOptions: number[] = [5, 10, 20];
  itemsPerPage: number = 10;
  currentPage: number = 0;

  /** Agrandissement au clic sur une vignette : `null` = aucune. */
  zoomedImage: string | null = null;

  readonly tabs: GravityTab[] = [
    { label: 'Toutes', code: null },
    { label: 'Danger', code: 'DANGER' },
    { label: 'Avertissement', code: 'WARNING' },
    { label: 'Information', code: 'INFO' },
  ];
  activeTab: string | null = null;

  /**
   * Toutes les alertes, chargées en une fois.
   *
   * <p>Les onglets de filtre des maquettes affichent un décompte PAR STATUT et filtrent
   * l'ensemble, pas la page courante. Or `/v1/alerts` n'accepte que `page`/`size` : aucun filtre
   * côté serveur. Filtrer la seule page affichée aurait donné des compteurs faux et un filtre
   * trompeur ; on charge donc la liste complète (`/v1/alerts/s`) et on filtre/pagine côté client.
   * Acceptable pour ce volume (une alerte par débordement constaté) — à repasser côté serveur si
   * la table grossit, ce qui demandera un paramètre de filtre sur l'API.
   */
  private allAlerts: any[] = [];

  constructor (
    private sharedService: SharedService,
    private headerTitleServie: headerTitleService,
    private modalServie: ModalService,
    private _liveAnnouncer: LiveAnnouncer,
    private columnPrefs: ColumnPreferencesService,
  ) {}

  ngOnInit() {
    this.displayedColumns = this.columnPrefs.restore(
      AlertComponent.COLUMNS_KEY, this.manageableColumns);
    this.headerTitleServie.setTitle('Gestion des alertes');
    this.loadAlerts();
  }

  loadAlerts() {
    this.sharedService.url = API_ENDPOINTS.alerts.listPath;
    this.sharedService.getAll().subscribe((alerts: any[]) => {
      this.allAlerts = alerts ?? [];
      this.applyTab();
    });
  }

  /** Nombre d'alertes d'une gravité — alimente les compteurs des onglets. */
  countFor(code: string | null): number {
    return code === null
      ? this.allAlerts.length
      : this.allAlerts.filter(a => a.code === code).length;
  }

  selectTab(code: string | null): void {
    this.activeTab = code;
    this.currentPage = 0;
    this.applyTab();
  }

  private applyTab(): void {
    const filtered = this.activeTab === null
      ? this.allAlerts
      : this.allAlerts.filter(a => a.code === this.activeTab);

    this.totalAlerts = filtered.length;
    this.totalPages = Math.max(1, Math.ceil(this.totalAlerts / this.itemsPerPage));
    if (this.currentPage >= this.totalPages) {
      this.currentPage = this.totalPages - 1;
    }
    const start = this.currentPage * this.itemsPerPage;
    this.dataSource.data = filtered.slice(start, start + this.itemsPerPage);
    this.dataSource.sort = this.sort;
  }

  onPaginatedChange(event: { pageIndex: number, pageSize: number }) {
    this.currentPage = event.pageIndex;
    this.itemsPerPage = event.pageSize;
    this.applyTab();
  }

  goToPage(pageIndex: number): void {
    if (pageIndex < 0 || pageIndex >= this.totalPages) { return; }
    this.onPaginatedChange({ pageIndex, pageSize: this.itemsPerPage });
  }

  openCreateAlertModal() {
    this.modalServie.openModal(CreateAlertComponent)
      .afterClosed().subscribe(() => this.loadAlerts());
  }

  openUpdateAlertModal(id: any) {
    const currentAlert = this.allAlerts.find((item: any) => item.alertId === id);
    this.modalServie.openModal(CreateAlertComponent, { id: id, currentAlert: currentAlert })
      .afterClosed().subscribe(() => this.loadAlerts());
  }

  openDeleteAlertModal(id: any) {
    this.modalServie.openModal(DeleteComponent, { id: id, url: API_ENDPOINTS.alerts.basePath })
      .afterClosed().subscribe(() => this.loadAlerts());
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  /** Classes de l'étiquette de gravité — DANGER/WARNING/INFO sont les seules valeurs d'`AlertCode`. */
  codeClasses(code: string): string {
    switch (code) {
      case 'DANGER': return 'sw-badge sw-badge--danger';
      case 'WARNING': return 'sw-badge sw-badge--warn';
      default: return 'sw-badge sw-badge--ok';
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

  // Lightbox au clic plutôt que l'ancien agrandissement au survol, qui repositionnait la vignette
  // en absolute avec un `scale(8)` : l'image sortait de la ligne et laissait un trou dans la cellule.
  openZoom(element: any): void {
    if (element?.image?.data) {
      this.zoomedImage = 'data:image/png;base64,' + element.image.data;
    }
  }

  closeZoom(): void {
    this.zoomedImage = null;
  }

  openColumnManager(): void {
    this.columnPrefs.open(AlertComponent.COLUMNS_KEY, this.manageableColumns, this.displayedColumns)
      .subscribe(columns => { if (columns) { this.displayedColumns = columns; } });
  }
}
