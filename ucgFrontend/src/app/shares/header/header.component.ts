import { Component, ElementRef, OnInit, Renderer2, ViewChild } from '@angular/core';
declare var $: any;

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})

export class HeaderComponent {

  @ViewChild('navbar') elRefs: ElementRef | undefined;
  
  constructor(
    private elRef: ElementRef,
    private renderer: Renderer2,
  ) { }

    ngOnInit(): void {

  }

  jquery(event: any): void{


   
    const el = this.elRef.nativeElement.querySelector('#navigation');

    this.renderer.addClass(el, 'active');

    $('#sidebarCollapse').on('click', () => {
        $('#sidebar').toggleClass('active');
    });

  }

}
  


