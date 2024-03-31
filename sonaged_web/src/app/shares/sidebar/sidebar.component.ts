import { Component, ElementRef, OnInit, Renderer2, ViewChild } from '@angular/core';
declare var $: any;

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent {

  //@ViewChild('sidebar') elRefs: ElementRef;

  constructor(
    private elRef: ElementRef,
    private renderer: Renderer2
  ) { }

  ngAfterViewInit(){
    this.logoutBtn();
  }

  jquery(event: any): void{
    const el = this.elRef.nativeElement.querySelector('#sidebar');

    this.renderer.addClass(el, 'active');

    $('#sidebarCollapse').on('click', () => {
        $('#sidebar').toggleClass('active');
    });

  }

  //get height element
  logoutBtn() {
    const el = this.elRef.nativeElement.querySelector('#sidebar');
    const height = el.offsetWidth; // Get the height of the element
    console.log("Height of the element: " + height);
  }
  
}
