import { AfterViewInit, Component, ElementRef, OnInit, Renderer2, ViewChild } from '@angular/core';
import { sidebarAnimations } from './sidebar.animations';
declare var $: any;

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
  animations: sidebarAnimations
})
export class SidebarComponent implements AfterViewInit{

  //@ViewChild('sidebar') elRefs: ElementRef;

  constructor(
    private elRef: ElementRef,
    private renderer: Renderer2
  ) { }

  ngAfterViewInit(){
  //this.logoutBtn();
  }


  //get height element
  logoutBtn() {
    const el = this.elRef.nativeElement.querySelector('#sidebar');
    const height = el.offsetWidth; // Get the height of the element
    console.log("Height of the element: " + height);
  }
  
}
