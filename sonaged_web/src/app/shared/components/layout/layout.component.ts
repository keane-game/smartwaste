import { Component, ElementRef, Renderer2 ,AfterContentChecked, ChangeDetectorRef } from '@angular/core';

declare var $: any;

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent implements AfterContentChecked {
isClicked = false;


  constructor(
    private elRef: ElementRef,
    private renderer: Renderer2,
    private changeDetector: ChangeDetectorRef,
  ) {}

 


  handleSidebarToggles(event: any): void{
    this.isClicked = !this.isClicked;
    const el = this.elRef.nativeElement.querySelector('#sidebar');
    const el2 = this.elRef.nativeElement.querySelectorAll('.main-content');
    const body = this.elRef.nativeElement.querySelector('.main-body');
    console.log("ell:"+ el)
    el2.forEach((element: any) => {
     
        this.renderer.removeClass(element, 'toggle-sidebar');
    });
    if(this.isClicked){
      this.renderer.addClass(body, 'toggle-sidebar');
      el2.forEach((element: any) => {
    });
    }else{
      this.renderer.removeClass(body, 'toggle-sidebar');
      el2.forEach((element: any) => {
    });
    }
    //console.log("toggle-sidebar-btn")
  }



  ngAfterContentChecked(): void {
    this.changeDetector.detectChanges();
  }

}
