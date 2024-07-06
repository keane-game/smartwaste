import { Component, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { SharedService } from '../../../services/shared.service';
import { headerTitleService } from '../../../services/headerTitle.service';
import { ModalService } from '../../../services/modal.service';
import { CreateAlertComponent } from '../create-alert/create-alert.component';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { DeleteComponent } from '../../../shared/components/delete/delete.component';

@Component({
  selector: 'app-alert',
  templateUrl: './alert.component.html',
  styleUrl: './alert.component.scss'
})
export class AlertComponent {

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = [ 'picture', 'object', 'message', 'code',  'adress', 'action'];
  dataSource = new MatTableDataSource<any>([]);


  alerts:  any;
  totalPages: number = 1;
  totalAlerts: number = 0;
  pageSizeOptions: number[] = [5,10, 20];
  itemsPerPage: number = 5;
  currentPage: number = 0;
  zoomStyle = {};
  constructor (
    private sharedService: SharedService,
    private headerTitleServie: headerTitleService,
    private modalServie: ModalService,
    private _liveAnnouncer: LiveAnnouncer

  ){}

  ngOnInit(){
    this.sharedService.url = '/alerts';
    this.loadAlerts(this.currentPage, this.itemsPerPage);
    this.headerTitleServie.setTitle('Gestion Alert');
  }

  loadAlerts(page: number, size: number) {
    this.sharedService.getResources(page, size).subscribe((resp) => {
      this.dataSource.data = resp.content;
      this.totalAlerts = resp.totalElements;
      this.totalPages = Math.ceil(this.totalAlerts / this.itemsPerPage);
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
      console.log(resp.content);
  
    })
  }
  
  onPaginatedChange(event: { pageIndex: number, pageSize: number }) {
    this.currentPage = event.pageIndex;
    this.itemsPerPage = event.pageSize;
    this.loadAlerts(this.currentPage, this.itemsPerPage);
   // console.log(event);
  }

  openCreateAlertModal(){
    this.modalServie.openModal(CreateAlertComponent)
  }

  openUpdateAlertModal(id: any){
    const currentAlert = this.dataSource.data.find((item: any ) => item.alertId === id);
    this.modalServie.openModal(CreateAlertComponent, {id: id, currentAlert: currentAlert})
  }

  openDeleteAlertModal(id: any){
    this.modalServie.openModal(DeleteComponent, {id: id, url: this.sharedService.url })
  }

  /** Announce the change in sort state for assistive technology. */
  announceSortChange(sortState: Sort) {
    if (sortState.direction) {
      this._liveAnnouncer.announce(`Sorted ${sortState.direction}ending`);
    } else {
      this._liveAnnouncer.announce('Sorting cleared');
    }
  }
 

  // onMouseMove(event: MouseEvent, zoomStyle: any) {
  //   const imageContainer = event.currentTarget as HTMLElement;
  //   const rect = imageContainer.getBoundingClientRect();
  //   const x = event.clientX - rect.left; // x position within the container
  //   const y = event.clientY - rect.top;  // y position within the container

  //   // this.zoomStyle = {
  //   //   'transform-origin': `${x}px ${y}px`,
  //   //   'transform': 'scale(1.5)' // Adjust the scale factor as needed
  //   // };
  //   zoomStyle['transform-origin'] = `${x}px ${y}px`;
  //   zoomStyle['transform'] = 'scale(1.5)'; 
  // }

  // onMouseLeave(zoomStyle: any) {
  //   // this.zoomStyle = {
  //   //   'transform-origin': 'center center',
  //   //   'transform': 'scale(1)'
  //   // };

  //   zoomStyle['transform-origin'] = 'center center';
  //   zoomStyle['transform'] = 'scale(1)';
  // }

  onMouseMove(event: MouseEvent) {
    const imageContainer = event.currentTarget as HTMLElement;
    const image = imageContainer.querySelector('img') as HTMLElement;
    const rect = image.getBoundingClientRect();
    const x = event.clientX / 4; // x position within the container
    const y = event.clientY /3;  // y position within the container
// Calculate percentage position within the image=

const bodyRect = document.body.getBoundingClientRect();
//console.log(bodyRect)

console.log(rect)
    image.style.position = 'absolute'
const posX = ((event.clientX - bodyRect.left) / bodyRect.width) * 100;
const posY = ((event.clientY - bodyRect.top) / bodyRect.height) * 100;

//image.style.transformOrigin = `${posX}% ${posY}%`;
image.style.top = '40%'
image.style.left = '40%'
  image.style.bottom = '0px'
image.style.right = '0px'
    image.style.position = 'absolute'
    image.style.transformOrigin = `${30}% ${50}%`;
    image.style.transform = 'scale(8)'; // Adjust the scale factor as needed
    //image.style.transform = ' translate(12px, 70%)'
  }

  onMouseLeave(event: MouseEvent) {
    const imageContainer = event.currentTarget as HTMLElement;
    const image = imageContainer.querySelector('img') as HTMLElement;
   // image.style.transformOrigin = 'center center';
    image.style.transform = 'scale(1)';
    image.style.top = '0px'
image.style.left = '0px'
    image.style.position = 'relative'
  }

}
