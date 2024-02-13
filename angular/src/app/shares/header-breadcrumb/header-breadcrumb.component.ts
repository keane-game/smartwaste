import { Component, OnInit } from '@angular/core';
import { RouteStateService } from 'src/app/core/services/route-state.service';

@Component({
  selector: 'app-header-breadcrumb',
  templateUrl: 'header-breadcrumb.component.html',
  styleUrls: ['header-breadcrumb.component.scss']
})
export class HeaderBreadcrumbComponent  {



  constructor(private routeStateService: RouteStateService) {
   
  }


}
