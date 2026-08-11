import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { SharedModule } from '../../shared/shared.module';
import { SharedService } from '../../services/shared.service';
import { API_ENDPOINTS } from '../../shared/constants/api-endpoints';
import { headerTitleService } from '../../services/headerTitle.service';
import { ModalService } from '../../services/modal.service';
import { DeleteComponent } from '../../shared/components/delete/delete.component';
import { CreateRegionComponent } from './create-region/create-region.component';

// Écran jamais construit auparavant (stub CLI "region works!") : la région n'apparaît nulle part
// dans le menu (sidebar.component.ts) — un département/Pikine unique dans les données réelles
// rend la hiérarchie Région superflue au quotidien — mais la route existe et le backend expose un
// CRUD complet (GET/POST/PUT/DELETE), donc autant que l'écran fonctionne plutôt que d'afficher un
// stub si quelqu'un tape l'URL directement.
@Component({
  selector: 'app-region',
  standalone: true,
  imports: [CommonModule, SharedModule],
  templateUrl: './region.component.html',
  styleUrl: './region.component.scss',
})
export class RegionComponent {

  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = ['regionName', 'regionCode', 'departments', 'action'];
  dataSource = new MatTableDataSource<any>([]);

  totalPages: number = 1;
  totalRegions: number = 0;
  itemsPerPage: number = 10;
  pageSizeOptions: number[] = [5, 10, 20];
  currentPage: number = 0;

  constructor(
    private sharedService: SharedService,
    private modalService: ModalService,
    private _liveAnnouncer: LiveAnnouncer,
    private headerTitleService: headerTitleService,
  ) { }

  ngOnInit() {
    this.sharedService.url = API_ENDPOINTS.regions.basePath;
    this.headerTitleService.setTitle('Gestion des régions');
    this.loadRegions(this.currentPage, this.itemsPerPage);
  }

  loadRegions(page: number = 0, size: number = 10): void {
    this.sharedService.getResources(page, size).subscribe((resp) => {
      this.dataSource.data = resp.content;
      this.totalRegions = resp.totalElements;
      this.totalPages = Math.ceil(this.totalRegions / this.itemsPerPage);
      this.dataSource.sort = this.sort;
    });
  }

  onPaginatedChange(event: { pageIndex: number, pageSize: number }) {
    this.currentPage = event.pageIndex;
    this.itemsPerPage = event.pageSize;
    this.loadRegions(this.currentPage, this.itemsPerPage);
  }

  openCreateRegionModal() {
    this.modalService.openModal(CreateRegionComponent, { title: 'Create Region' })
      .afterClosed().subscribe(() => this.loadRegions(this.currentPage, this.itemsPerPage));
  }

  openUpdateRegionModal(id: any) {
    const currentRegion = this.dataSource.data.find((item: any) => item.regionId === id);
    this.modalService.openModal(CreateRegionComponent, { id: id, currentRegion: currentRegion })
      .afterClosed().subscribe(() => this.loadRegions(this.currentPage, this.itemsPerPage));
  }

  openDeleteRegionModal(id: any) {
    this.modalService.openModal(DeleteComponent, { id: id, url: API_ENDPOINTS.regions.basePath })
      .afterClosed().subscribe(() => this.loadRegions(this.currentPage, this.itemsPerPage));
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  announceSortChange(sortState: Sort) {
    if (sortState.direction) {
      this._liveAnnouncer.announce(`Sorted ${sortState.direction}ending`);
    } else {
      this._liveAnnouncer.announce('Sorting cleared');
    }
  }
}
