import { Component, ElementRef, Renderer2, ViewChild } from '@angular/core';
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




  jquery(event: any): void{
    const el = this.elRef.nativeElement.querySelector('#sidebar');

    this.renderer.addClass(el, 'active');

    $('#sidebarCollapse').on('click', () => {
        $('#sidebar').toggleClass('active');
    });

  }
}
