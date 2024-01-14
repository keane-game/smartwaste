import { Component, ElementRef, Renderer2 } from '@angular/core';

declare var $: any;

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent {
isClicked = false;
pageTitle = "Dashboard"

  constructor(
    private elRef: ElementRef,
    private renderer: Renderer2
  ) { }

  jquery(event: any): void{
    this.isClicked = !this.isClicked;
    const el = this.elRef.nativeElement.querySelector('#sidebar');
    const el2 = this.elRef.nativeElement.querySelectorAll('.main-content');
    const body = this.elRef.nativeElement.querySelector('.main-body');
    console.log("ell:"+ body)
    el2.forEach((element: any) => {
     
        this.renderer.removeClass(element, 'toggle-sidebar');
    });
    if(this.isClicked){
      this.renderer.addClass(el, 'active');
      this.renderer.addClass(body, 'toggle-sidebar');
      el2.forEach((element: any) => {
      //  this.renderer.addClass(element, 'toggle-sidebar');
    });
    }else{
      this.renderer.removeClass(el, 'active');
      this.renderer.removeClass(body, 'toggle-sidebar');
      el2.forEach((element: any) => {
       // this.renderer.removeClass(element, 'toggle-sidebar');
    });
    }
    //console.log("toggle-sidebar-btn")
  }

  
}
