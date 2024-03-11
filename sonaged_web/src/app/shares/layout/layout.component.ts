import { AfterContentChecked, AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnInit, Renderer2 } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Router, ActivatedRoute } from '@angular/router';
import { headerTitleService } from '../../services/headerTitle.service'; ;
import * as Leaflet from 'leaflet';

declare var $: any;

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent  implements  AfterViewInit, OnInit, AfterContentChecked {
isClicked = false;
pageTitle = "Dashboard"


options: Leaflet.MapOptions = {
  layers: getLayers(),
  zoom: 20,
  center: new Leaflet.LatLng(14.693425, -17.447938, 20)
};
  constructor(
    private elRef: ElementRef,
    private renderer: Renderer2,

    private router: Router,
    private titleService: Title,
    private route: ActivatedRoute,
    private changeDetector: ChangeDetectorRef,
    private headerTitleService: headerTitleService
  ) {}

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
      this.renderer.addClass(body, 'toggle-sidebar');
      el2.forEach((element: any) => {
      //  this.renderer.addClass(element, 'toggle-sidebar');
    });
    }else{
      this.renderer.removeClass(body, 'toggle-sidebar');
      el2.forEach((element: any) => {
       // this.renderer.removeClass(element, 'toggle-sidebar');
    });
    }
    //console.log("toggle-sidebar-btn")
  }

  
  ngOnInit() {
    console.log("ell:"+ this.pageTitle)
  }

  ngAfterContentChecked(): void {
    this.changeDetector.detectChanges();
  }

  ngAfterViewInit(): void {
    Promise.resolve().then(()=> {
      this.headerTitleService.title.subscribe(updatedTitle => {
        this.pageTitle = updatedTitle;
        console.log("ell:"+ updatedTitle)
      });
    })

    console.log("ell:"+ this.pageTitle)

  }


}

export const getLayers = (): Leaflet.Layer[] => {
  return [
    new Leaflet.TileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    } as Leaflet.TileLayerOptions),
  ] as Leaflet.Layer[];
};